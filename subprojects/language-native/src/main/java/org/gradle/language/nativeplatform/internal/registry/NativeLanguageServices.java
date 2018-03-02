/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.nativeplatform.internal.registry;

import com.google.common.base.Objects;
import org.gradle.internal.service.ServiceRegistration;
import org.gradle.internal.service.scopes.AbstractPluginServiceRegistry;
import org.gradle.language.cpp.internal.NativeDependencyCache;
import org.gradle.language.internal.DefaultNativeComponentFactory;
import org.gradle.language.nativeplatform.internal.incremental.DefaultCompilationStateCacheFactory;
import org.gradle.language.nativeplatform.internal.incremental.DefaultIncrementalCompilerBuilder;
import org.gradle.language.nativeplatform.internal.incremental.IncrementalCompileFilesFactory;
import org.gradle.language.nativeplatform.internal.incremental.sourceparser.CachingCSourceParser;
import org.gradle.language.nativeplatform.internal.toolchains.DefaultToolChainSelector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NativeLanguageServices extends AbstractPluginServiceRegistry {
    @Override
    public void registerGradleServices(ServiceRegistration registration) {
        registration.add(DefaultCompilationStateCacheFactory.class);
        registration.add(CachingCSourceParser.class);
    }

    @Override
    public void registerBuildSessionServices(ServiceRegistration registration) {
        registration.add(IncludeAnalysisFileDetailsCache.class);
    }

    @Override
    public void registerBuildServices(ServiceRegistration registration) {
        registration.add(NativeDependencyCache.class);
    }

    @Override
    public void registerProjectServices(ServiceRegistration registration) {
        registration.add(DefaultIncrementalCompilerBuilder.class);
        registration.add(DefaultToolChainSelector.class);
        registration.add(DefaultNativeComponentFactory.class);
    }

    public static class IncludeAnalysisFileDetailsCache {
        private final Map<File, IncrementalCompileFilesFactory.FileDetails> cache = new HashMap<File, IncrementalCompileFilesFactory.FileDetails>();
        private final Map<Integer, IncrementalCompileFilesFactory.FileDetails> cacheAccordingToMacros = new HashMap<Integer, IncrementalCompileFilesFactory.FileDetails>();
        private final Object lock = new Object();

        public IncrementalCompileFilesFactory.FileDetails get(File file, int hash) {
            synchronized (lock) {
                IncrementalCompileFilesFactory.FileDetails result = cache.get(file);
                if (result != null) {
                    return result;
                }

                return cacheAccordingToMacros.get(Objects.hashCode(file, hash));
            }
        }

        public void put(File file, IncrementalCompileFilesFactory.FileDetails fileDetails) {
            synchronized (lock) {
                cache.put(file, fileDetails);
            }
        }

        public void put(File file, int hash, IncrementalCompileFilesFactory.FileDetails fileDetails) {
            synchronized (lock) {
                cacheAccordingToMacros.put(Objects.hashCode(file, hash), fileDetails);
            }
        }
    }
}
