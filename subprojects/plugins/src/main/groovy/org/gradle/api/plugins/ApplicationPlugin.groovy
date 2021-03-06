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
package org.gradle.api.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.GradleException

/**
 * <p>A {@link Plugin} which runs a project as a Java Application.</p>
 *
 * @author Rene Groeschke
 */
class ApplicationPlugin implements Plugin<Project> {
    static final String APPLICATION_PLUGIN_NAME = "application"
    static final String APPLICATION_GROUP = APPLICATION_PLUGIN_NAME

    static final String TASK_RUN_NAME = "run"
    static final String TASK_START_SCRIPTS_NAME = "startScripts"
    static final String TASK_INSTALL_NAME = "installApp"
    static final String TASK_DIST_ZIP_NAME = "distZip"

    private Project project
    private ApplicationPluginConvention pluginConvention

    void apply(final Project project) {
        this.project = project
        project.plugins.apply(JavaPlugin)

        addPluginConvention()
        addRunTask()
        addCreateScriptsTask()
        addInstallTask()
        addDistZipTask()
    }

    private void addPluginConvention() {
        pluginConvention = new ApplicationPluginConvention()
        pluginConvention.applicationName = project.name
        project.convention.plugins.application = pluginConvention
    }

    private void addRunTask() {
        def run = project.tasks.add(TASK_RUN_NAME, JavaExec)
        run.description = "Runs this project as a JVM application"
        run.group = APPLICATION_GROUP
        run.classpath = project.sourceSets.main.runtimeClasspath
        run.conventionMapping.main = { pluginConvention.mainClassName }
    }

    // @Todo: refactor this task configuration to extend a copy task and use replace tokens
    private void addCreateScriptsTask() {
        def startScripts = project.tasks.add(TASK_START_SCRIPTS_NAME, CreateStartScripts)
        startScripts.description = "Creates OS specific scripts to run the project as a JVM application."
        startScripts.classpath = project.tasks[JavaPlugin.JAR_TASK_NAME].outputs.files + project.configurations.runtime
        startScripts.conventionMapping.mainClassName = { pluginConvention.mainClassName }
        startScripts.conventionMapping.applicationName = { pluginConvention.applicationName }
        startScripts.conventionMapping.outputDir = { new File(project.buildDir, 'scripts') }
    }

    private void addInstallTask() {
        def installTask = project.tasks.add(TASK_INSTALL_NAME, Sync)
        installTask.description = "Installs the project as a JVM application along with libs and OS specific scripts."
        installTask.group = APPLICATION_GROUP
        installTask.with(createDistSpec())
        installTask.into { project.file("${project.buildDir}/install/${pluginConvention.applicationName}") }
        installTask.doFirst {
            if (destinationDir.directory) {
                if (!new File(destinationDir, 'lib').directory || !new File(destinationDir, 'bin').directory) {
                    throw new GradleException("The specified installation directory '${destinationDir}' is neither empty nor does it contain an installation for '${pluginConvention.applicationName}'.\n" +
                            "If you really want to install to this directory, delete it and run the install task again.\n" +
                            "Alternatively, choose a different installation directory."
                    )
                }
            }
        }
        installTask.doLast {
            project.ant.chmod(file: "${destinationDir.absolutePath}/bin/${pluginConvention.applicationName}", perm: 'ugo+x')
        }
    }

    private void addDistZipTask() {
        def distZipTask = project.tasks.add(TASK_DIST_ZIP_NAME, Zip)
        distZipTask.description = "Bundles the project as a JVM application with libs and OS specific scripts."
        distZipTask.group = APPLICATION_GROUP
        distZipTask.conventionMapping.baseName = { pluginConvention.applicationName }
        def baseDir = { distZipTask.archiveName - ".zip" }
        distZipTask.into(baseDir) {
            with(createDistSpec())
        }
    }

    private CopySpec createDistSpec() {
        def jar = project.tasks[JavaPlugin.JAR_TASK_NAME]
        def startScripts = project.tasks[TASK_START_SCRIPTS_NAME]

        project.copySpec {
            into("lib") {
                from(jar.outputs.files)
                from(project.configurations.runtime)
            }
            into("bin") {
                from(startScripts.outputs.files)
                fileMode = 0755
            }
        }
    }
}