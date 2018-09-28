package com.emarsys.mobileengage.inbox;

import com.emarsys.core.api.result.CompletionListener;
import com.emarsys.core.api.result.ResultListener;
import com.emarsys.core.api.result.Try;
import com.emarsys.mobileengage.api.inbox.Notification;
import com.emarsys.mobileengage.api.inbox.NotificationInboxStatus;

public interface InboxInternal {
    void fetchNotifications(ResultListener<Try<NotificationInboxStatus>> resultListener);

    void resetBadgeCount(CompletionListener resultListener);

    void trackNotificationOpen(Notification message, CompletionListener resultListener);

    void purgeNotificationCache();
}
