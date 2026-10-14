package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell threshold. */
data class DwellThresholdView(
    var codeText: String,
    var label: String,
    var summaryText: String,
    var updatedAt: String,
    var discount: String,
    var statusLabel: String,
)

fun toDwellThresholdView(item: DwellThreshold): DwellThresholdView {
    return DwellThresholdView(
        codeText = truncate(item.code, 120),
        label = item.label,
        summaryText = truncate(item.summary, 40),
        updatedAt = item.updatedAt.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellThresholdViews(items: MutableList<DwellThreshold>): MutableList<DwellThresholdView> {
    val views = mutableListOf<DwellThresholdView>()
    for (item in items) {
        views.add(toDwellThresholdView(item))
    }
    return views
}

fun sumDwellThresholdView(view: DwellThresholdView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedUpdatedAt.toDouble() + parsedDiscount
}
