/**
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.gson.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.gson.GsonPlugin;

/**
 * Gson method interceptor
 *
 * @author ChaYoung You
 */
public class FromJsonInterceptor implements Interceptor {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public FromJsonInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    public void before(Object target, Object arg0, Object arg1) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, new Object[] {arg0, arg1});
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
    }

    public void after(Object target, Object result, Throwable throwable, Object arg0, Object arg1) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, new Object[] {arg0, arg1}, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordServiceType(GsonPlugin.GSON_SERVICE_TYPE);
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);

            if (arg0 != null && arg0 instanceof String) {
                recorder.recordAttribute(GsonPlugin.GSON_ANNOTATION_KEY_JSON_LENGTH, ((String) arg0).length());
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}