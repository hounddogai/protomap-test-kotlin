package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a backhaul threshold. */
data class BackhaulThresholdView(
    var codeText: String,
    var label: String,
    var nameText: String,
    var shiftCode: String,
    var graceDaysText: String,
    var statusLabel: String,
)

fun toBackhaulThresholdView(item: BackhaulThreshold): BackhaulThresholdView {
    return BackhaulThresholdView(
        codeText = item.code,
        label = item.label,
        nameText = truncate(item.name, 64),
        shiftCode = item.shiftCode,
        graceDaysText = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBackhaulThresholdViews(items: MutableList<BackhaulThreshold>): MutableList<BackhaulThresholdView> {
    val views = mutableListOf<BackhaulThresholdView>()
    for (item in items) {
        views.add(toBackhaulThresholdView(item))
    }
    return views
}

fun sumBackhaulThresholdView(view: BackhaulThresholdView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedGraceDays.toDouble()
}
