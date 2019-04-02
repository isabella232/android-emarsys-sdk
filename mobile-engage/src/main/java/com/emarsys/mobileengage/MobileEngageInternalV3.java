package com.emarsys.mobileengage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.request.RequestManager;
import com.emarsys.core.request.model.RequestModel;
import com.emarsys.core.util.Assert;
import com.emarsys.mobileengage.event.applogin.AppLoginParameters;
import com.emarsys.mobileengage.request.RequestModelFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MobileEngageInternalV3 implements MobileEngageInternal {

    private final RequestManager requestManager;
    private final Handler uiHandler;
    private final RequestModelFactory requestModelFactory;
    private final RequestContext requestContext;

    public MobileEngageInternalV3(RequestManager requestManager,
                                  Handler uiHandler,
                                  RequestModelFactory requestModelFactory,
                                  RequestContext requestContext) {
        Assert.notNull(requestManager, "RequestManager must not be null!");
        Assert.notNull(uiHandler, "UiHandler must not be null!");
        Assert.notNull(requestModelFactory, "RequestModelFactory must not be null!");
        Assert.notNull(requestContext, "RequestContext must not be null!");

        this.requestManager = requestManager;
        this.uiHandler = uiHandler;
        this.requestModelFactory = requestModelFactory;
        this.requestContext = requestContext;
    }

    @Override
    public void setPushToken(String pushToken, CompletionListener completionListener) {
        if (pushToken != null) {
            RequestModel requestModel = requestModelFactory.createSetPushTokenRequest(pushToken);

            requestManager.submit(requestModel, completionListener);
        }
    }

    @Override
    public void removePushToken(CompletionListener completionListener) {

    }

    @Override
    public void setContact(String contactFieldValue, CompletionListener completionListener) {
        requestContext.setAppLoginParameters(new AppLoginParameters(requestContext.getContactFieldId(), contactFieldValue));
        RequestModel requestModel = requestModelFactory.createSetContactRequest(contactFieldValue);
        requestManager.submit(requestModel, completionListener);
    }

    @Override
    public String clearContact(CompletionListener completionListener) {
        setContact(null, completionListener);
        return null;
    }

    @Override
    public String trackCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = requestModelFactory.createCustomEventRequest(eventName, eventAttributes);
        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }

    @Override
    public String trackInternalCustomEvent(String eventName, Map<String, String> eventAttributes, CompletionListener completionListener) {
        Assert.notNull(eventName, "EventName must not be null!");

        RequestModel requestModel = requestModelFactory.createInternalCustomEventRequest(eventName, eventAttributes);
        requestManager.submit(requestModel, completionListener);

        return requestModel.getId();
    }

    @Override
    public void trackMessageOpen(Intent intent, final CompletionListener completionListener) {
        Assert.notNull(intent, "Intent must not be null!");

        String messageId = getMessageId(intent);

        if (messageId != null) {
            handleMessageOpen(completionListener, messageId);
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    completionListener.onCompleted(new IllegalArgumentException("No messageId found!"));
                }
            });
        }
    }

    String getMessageId(Intent intent) {
        String sid = null;
        Bundle payload = intent.getBundleExtra("payload");
        if (payload != null) {
            String customData = payload.getString("u");
            if (customData != null) {
                try {
                    sid = new JSONObject(customData).getString("sid");
                } catch (JSONException ignore) {
                }
            }
        }
        return sid;
    }

    private void handleMessageOpen(CompletionListener completionListener, String messageId) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("sid", messageId);
        attributes.put("origin", "main");
        RequestModel requestModel = requestModelFactory.createInternalCustomEventRequest("push:click", attributes);
        requestManager.submit(requestModel, completionListener);
    }

    @Override
    public void trackDeviceInfo() {
        RequestModel requestModel = requestModelFactory.createTrackDeviceInfoRequest();

        requestManager.submit(requestModel, null);
    }

}
