package com.emarsys.mobileengage.inbox;

import com.emarsys.core.request.RequestManager;
import com.emarsys.mobileengage.RequestContext;
import com.emarsys.mobileengage.request.MobileEngageRequestModelFactory;

public class InboxInternalProvider {

    public InboxInternal provideInboxInternal(
            RequestManager requestManager,
            RequestContext requestContext,
            MobileEngageRequestModelFactory requestModelFactory) {
        return new DefaultInboxInternal(requestManager, requestContext, requestModelFactory);
    }

    public InboxInternal provideLoggingInboxInternal(Class klass){
        return new LoggingInboxInternal(klass);
    }
}
