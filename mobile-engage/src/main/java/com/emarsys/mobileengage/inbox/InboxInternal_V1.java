package com.emarsys.mobileengage.inbox;

import android.os.Handler;
import android.os.Looper;

import com.emarsys.core.CoreCompletionHandler;
import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.RestClient;
import com.emarsys.core.request.model.RequestMethod;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.response.ResponseModel;
import com.emarsys.core.util.Assert;
import com.emarsys.core.util.log.EMSLogger;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.api.MobileEngageException;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;
import com.emarsys.mobileengage.inbox.model.NotificationCache;
import com.emarsys.mobileengage.util.RequestHeaderUtils;
import com.emarsys.mobileengage.util.RequestPayloadUtils;
import com.emarsys.mobileengage.util.RequestUrlUtils;
import com.emarsys.mobileengage.util.log.MobileEngageTopic;

import java.util.HashMap;
import java.util.Map;

import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_FETCH_V1;
import static com.emarsys.mobileengage.endpoint.Endpoint.INBOX_RESET_BADGE_COUNT_V1;

public class InboxInternal_V1 implements InboxInternal {

    Handler handler;
    RestClient client;
    NotificationCache cache;
    RequestManager manager;
    RequestContext requestContext;

    public InboxInternal_V1(
            RequestManager requestManager,
            RestClient restClient,
            RequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(restClient, "RestClient must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: requestContext %s, requestManager %s", requestContext, requestManager);

        this.client = restClient;
        this.handler = new Handler(Looper.getMainLooper());
        this.cache = new NotificationCache();
        this.manager = requestManager;
        this.requestContext = requestContext;
    }

    @Override
    public void fetchNotifications(final ResultListener<Try<NotificationInboxStatus>> resultListener) {
        Assert.notNull(resultListener, "ResultListener should not be null!");
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", resultListener);

        if (requestContext.getAppLoginParameters() != null && requestContext.getAppLoginParameters().hasCredentials()) {
            handleFetchRequest(resultListener);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onResult(Try.failure(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!")));
                }
            });
        }
    }

    private void handleFetchRequest(final ResultListener<Try<NotificationInboxStatus>> resultListener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_FETCH_V1)
                .headers(createBaseHeaders(requestContext))
                .method(RequestMethod.GET)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                NotificationInboxStatus status = InboxParseUtils.parseNotificationInboxStatus(responseModel.getBody());
                NotificationInboxStatus resultStatus = new NotificationInboxStatus(cache.merge(status.getNotifications()), status.getBadgeCount());
                resultListener.onResult(Try.success(resultStatus));
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                resultListener.onResult(Try.failure(new MobileEngageException(
                        responseModel.getStatusCode(),
                        responseModel.getMessage(),
                        responseModel.getBody())));
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                resultListener.onResult(Try.failure(cause));
            }
        });
    }

    @Override
    public void resetBadgeCount(final CompletionListener resultListener) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: resultListener %s", resultListener);
        if (requestContext.getAppLoginParameters() != null && requestContext.getAppLoginParameters().hasCredentials()) {
            handleResetRequest(resultListener);
        } else {
            if (resultListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        resultListener.onCompleted(new NotificationInboxException("AppLogin must be called before calling fetchNotifications!"));
                    }
                });
            }
        }
    }

    @Override
    public void trackNotificationOpen(Notification message, CompletionListener resultListener) {
        EMSLogger.log(MobileEngageTopic.INBOX, "Argument: %s", message);

        Map<String, Object> payload = RequestPayloadUtils.createBasePayload(requestContext);
        payload.put("source", "inbox");
        payload.put("sid", message.getSid());
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(RequestUrlUtils.createEventUrl_V2("message_open"))
                .payload(payload)
                .headers(RequestHeaderUtils.createBaseHeaders_V2(requestContext))
                .build();

        manager.submit(model);
    }

    @Override
    public void purgeNotificationCache() {

    }

    private void handleResetRequest(final CompletionListener resultListener) {
        RequestModel model = new RequestModel.Builder(requestContext.getTimestampProvider(), requestContext.getUUIDProvider())
                .url(INBOX_RESET_BADGE_COUNT_V1)
                .headers(createBaseHeaders(requestContext))
                .method(RequestMethod.POST)
                .build();

        client.execute(model, new CoreCompletionHandler() {
            @Override
            public void onSuccess(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (resultListener != null) {
                    resultListener.onCompleted(null);
                }
            }

            @Override
            public void onError(String id, ResponseModel responseModel) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, responseModel %s", id, responseModel);
                if (resultListener != null) {
                    resultListener.onCompleted(new MobileEngageException(
                            responseModel.getStatusCode(),
                            responseModel.getMessage(),
                            responseModel.getBody()));
                }
            }

            @Override
            public void onError(String id, Exception cause) {
                EMSLogger.log(MobileEngageTopic.INBOX, "Arguments: id %s, cause %s", id, cause);
                if (resultListener != null) {
                    resultListener.onCompleted(cause);
                }
            }
        });
    }

    private Map<String, String> createBaseHeaders(RequestContext requestContext) {
        Map<String, String> result = new HashMap<>();

        result.put("x-ems-me-hardware-id", requestContext.getDeviceInfo().getHwid());
        result.put("x-ems-me-application-code", requestContext.getApplicationCode());
        result.put("x-ems-me-contact-field-id", String.valueOf(requestContext.getAppLoginParameters().getContactFieldId()));
        result.put("x-ems-me-contact-field-value", requestContext.getAppLoginParameters().getContactFieldValue());

        result.putAll(RequestHeaderUtils.createDefaultHeaders(requestContext));
        result.putAll(RequestHeaderUtils.createBaseHeaders_V2(requestContext));

        return result;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

}
