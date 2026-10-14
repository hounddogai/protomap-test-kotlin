package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance template. */
data class ClearanceTemplateView(
    var code: String,
    var titleText: String,
    var maximum: String,
    var name: String,
    var contactEmailText: String,
    var graceDays: String,
    var label: String,
    var localeText: String,
    var statusLabel: String,
)

fun toClearanceTemplateView(item: ClearanceTemplate): ClearanceTemplateView {
    return ClearanceTemplateView(
        code = truncate(item.code, 80),
        titleText = item.title,
        maximum = "%.2f".format(item.maximum),
        name = item.name,
        contactEmailText = item.contactEmail,
        graceDays = item.graceDays.toString(),
        label = truncate(item.label, 80),
        localeText = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceTemplateViews(items: MutableList<ClearanceTemplate>): MutableList<ClearanceTemplateView> {
    val views = mutableListOf<ClearanceTemplateView>()
    for (item in items) {
        views.add(toClearanceTemplateView(item))
    }
    return views
}

fun sumClearanceTemplateView(view: ClearanceTemplateView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMaximum + parsedGraceDays.toDouble()
}
