/*
 * Copyright 2016-2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.backend;

import org.osgi.service.log.LogService;

import no.priv.bang.ukelonn.UkelonnException;
import no.priv.bang.ukelonn.UkelonnService;

public class CommonServiceMethods {

    private CommonServiceMethods() {}

    public static UkelonnService connectionCheck(Class<?> clazz, UkelonnServiceProvider provider) {
        String className = clazz.getSimpleName();
        UkelonnService ukelonnService = provider;
        if (ukelonnService == null) {
            throw new UkelonnException(className + " bean unable to find OSGi service Ukelonnservice, giving up");
        }

        return ukelonnService;
    }

    public static void logError(Class<?> clazz, UkelonnServiceProvider provider, String message) {
        LogService logservice = logserviceConnectionCheck(clazz, provider);
        if (logservice != null) {
            logservice.log(LogService.LOG_ERROR, message);
        }
    }

    public static void logError(Class<?> clazz, UkelonnServiceProvider provider, String message, Throwable exception) {
        LogService logservice = logserviceConnectionCheck(clazz, provider);
        if (logservice != null) {
            logservice.log(LogService.LOG_ERROR, message, exception);
        }
    }

    private static LogService logserviceConnectionCheck(Class<?> clazz, UkelonnServiceProvider provider) {
        try {
            UkelonnService ukelonnService = connectionCheck(clazz, provider);

            return ukelonnService.getLogservice();
        } catch (RuntimeException e) {
            return null; // Don't fail if service is missing, just don't log anything
        }
    }

}
