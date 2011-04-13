/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.plugins.ide.eclipse

import org.gradle.integtests.fixtures.TestResources
import org.junit.Rule
import org.junit.Test

class EclipseNewModelIntegrationTest extends AbstractEclipseIntegrationTest {

    @Rule
    public final TestResources testResources = new TestResources()

    @Test
    void allowsConfiguringEclipseProject() {
        //when
        runEclipseTask """
apply plugin: 'java'
apply plugin: 'eclipse'

eclipse {
  project {
    name = 'someBetterName'
  }
}
        """

        //then
        println getFile([:], '.project').text

        def dotProject = parseProjectFile()
        assert dotProject.name.text() == 'someBetterName'

    }
}