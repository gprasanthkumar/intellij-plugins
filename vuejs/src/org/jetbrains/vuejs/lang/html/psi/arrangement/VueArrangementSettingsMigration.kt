// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.psi.arrangement

import com.intellij.application.options.codeStyle.CodeStyleSchemesModel
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.xml.arrangement.HtmlRearranger
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueArrangementSettingsMigration : StartupActivity, StartupActivity.DumbAware {


  override fun runActivity(project: Project) {
    val propertiesComponent = PropertiesComponent.getInstance(project)
    if (!propertiesComponent.isTrueValue(VUE_REARRANGER_SETTINGS_MIGRATION)) {
      propertiesComponent.setValue(VUE_REARRANGER_SETTINGS_MIGRATION, true)

      WriteAction.runAndWait<Throwable> {
        val codeStyleSchemesModel = CodeStyleSchemesModel(project)
        codeStyleSchemesModel.schemes.asSequence()
          .map { it.codeStyleSettings }
          .forEach { codeStyleSettings ->
            codeStyleSettings
              .getCommonSettings(HTMLLanguage.INSTANCE)
              .takeIf { it != HtmlRearranger().defaultSettings }
              ?.arrangementSettings
              ?.let {
                val vueSettings = codeStyleSettings
                  .getCommonSettings(VueLanguage.INSTANCE)
                if (vueSettings.arrangementSettings == null)
                  vueSettings.setArrangementSettings(it)
              }
          }
        codeStyleSchemesModel.apply()
      }
    }
  }

  companion object {
    const val VUE_REARRANGER_SETTINGS_MIGRATION = "vue.rearranger.settings.migration"
  }

}