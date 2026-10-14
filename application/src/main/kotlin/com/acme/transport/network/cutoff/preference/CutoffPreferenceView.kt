package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cutoff preference. */
data class CutoffPreferenceView(
    var code: String,
    var title: String,
    var visible: String,
    var dock: String,
    var version: String,
    var discount: String,
    var windowDays: String,
    var driverId: String,
    var statusLabel: String,
)

fun toCutoffPreferenceView(item: CutoffPreference): CutoffPreferenceView {
    return CutoffPreferenceView(
        code = item.code,
        title = item.title,
        visible = "${item.visible}",
        dock = truncate(item.dock, 40),
        version = item.version.toString(),
        discount = "%.2f".format(item.discount),
        windowDays = item.windowDays.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffPreferenceViews(items: MutableList<CutoffPreference>): MutableList<CutoffPreferenceView> {
    val views = mutableListOf<CutoffPreferenceView>()
    for (item in items) {
        views.add(toCutoffPreferenceView(item))
    }
    return views
}

fun sumCutoffPreferenceView(view: CutoffPreferenceView): Double {
    val parsedVersion = view.version.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedVersion.toDouble() + parsedDiscount + parsedWindowDays.toDouble()
}
