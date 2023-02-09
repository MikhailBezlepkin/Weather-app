package com.example.weatherapp

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources

import android.os.Build
import java.util.*


class LanguageConfig {
    fun iris(): Int {
        return 1
    }
     fun changeLanguage(context: Context, languageCode: String): ContextWrapper? {
        var context: Context = context
        val resources: Resources = context.getResources()
        val configuration: Configuration = resources.getConfiguration()
        val systemLocale: Locale
        systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.getLocales().get(0)
        } else {
            configuration.locale
        }
        if (languageCode != "" && !systemLocale.getLanguage().equals(languageCode)) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale)
            } else {
                configuration.locale = locale
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                context.getResources()
                    .updateConfiguration(configuration, context.getResources().getDisplayMetrics())
            }
        }
        return ContextWrapper(context)
    }
}