package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled qualification template. */
data class ScheduledQualificationTemplateView(
    var codeText: String,
    var label: String,
    var version: String,
    var currencyText: String,
    var pallets: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toScheduledQualificationTemplateView(item: ScheduledQualificationTemplate): ScheduledQualificationTemplateView {
    return ScheduledQualificationTemplateView(
        codeText = truncate(item.code, 40),
        label = item.label,
        version = item.version.toString(),
        currencyText = item.currency,
        pallets = item.pallets.toString(),
        phoneText = truncate(item.phone, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledQualificationTemplateViews(items: MutableList<ScheduledQualificationTemplate>): MutableList<ScheduledQualificationTemplateView> {
    val views = mutableListOf<ScheduledQualificationTemplateView>()
    for (item in items) {
        views.add(toScheduledQualificationTemplateView(item))
    }
    return views
}

fun sumScheduledQualificationTemplateView(view: ScheduledQualificationTemplateView): Double {
    val parsedVersion = view.version.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedVersion.toDouble() + parsedPallets.toDouble()
}
