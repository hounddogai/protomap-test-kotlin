package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection bundle. */
data class InspectionBundleView(
    var code: String,
    var title: String,
    var phone: String,
    var expiresAt: String,
    var windowDaysText: String,
    var category: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toInspectionBundleView(item: InspectionBundle): InspectionBundleView {
    return InspectionBundleView(
        code = item.code,
        title = item.title,
        phone = item.phone,
        expiresAt = item.expiresAt.toString(),
        windowDaysText = item.windowDays.toString(),
        category = truncate(item.category, 40),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionBundleViews(items: MutableList<InspectionBundle>): MutableList<InspectionBundleView> {
    val views = mutableListOf<InspectionBundleView>()
    for (item in items) {
        views.add(toInspectionBundleView(item))
    }
    return views
}

fun sumInspectionBundleView(view: InspectionBundleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedExpiresAt.toDouble() + parsedWindowDays.toDouble()
}
