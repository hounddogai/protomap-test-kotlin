package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled dispute bundle. */
data class ScheduledDisputeBundleView(
    var code: String,
    var label: String,
    var marginText: String,
    var updatedAt: String,
    var timezone: String,
    var durationMinutes: String,
    var visible: String,
    var category: String,
    var statusLabel: String,
)

fun toScheduledDisputeBundleView(item: ScheduledDisputeBundle): ScheduledDisputeBundleView {
    return ScheduledDisputeBundleView(
        code = truncate(item.code, 120),
        label = item.label,
        marginText = "%.2f".format(item.margin),
        updatedAt = item.updatedAt.toString(),
        timezone = item.timezone,
        durationMinutes = item.durationMinutes.toString(),
        visible = "${item.visible}",
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDisputeBundleViews(items: MutableList<ScheduledDisputeBundle>): MutableList<ScheduledDisputeBundleView> {
    val views = mutableListOf<ScheduledDisputeBundleView>()
    for (item in items) {
        views.add(toScheduledDisputeBundleView(item))
    }
    return views
}

fun sumScheduledDisputeBundleView(view: ScheduledDisputeBundleView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMargin + parsedUpdatedAt.toDouble() + parsedDurationMinutes.toDouble()
}
