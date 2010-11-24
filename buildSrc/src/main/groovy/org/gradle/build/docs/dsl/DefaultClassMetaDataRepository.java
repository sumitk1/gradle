/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.build.docs.dsl;

import org.gradle.build.docs.dsl.model.ClassMetaDataRepository;
import org.gradle.build.docs.dsl.model.ClassMetaData;

import java.util.Map;

public class DefaultClassMetaDataRepository implements ClassMetaDataRepository {
    private final Map<String, ClassMetaData> classes;

    public DefaultClassMetaDataRepository(Map<String, ClassMetaData> classes) {
        this.classes = classes;
    }

    public ClassMetaData findClass(String fullyQualifiedName) {
        ClassMetaData classMetaData = classes.get(fullyQualifiedName);
        if (classMetaData == null) {
            return null;
        }
        classMetaData.attach(this);
        return classMetaData;
    }
}