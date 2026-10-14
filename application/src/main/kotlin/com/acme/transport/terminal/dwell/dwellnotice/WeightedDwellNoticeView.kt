package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted dwell notice. */
data class WeightedDwellNoticeView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var capacity: String,
    var shiftCodeText: String,
    var pinned: String,
    var priority: String,
    var externalRef: String,
    var labelText: String,
    var margin: String,
    var statusLabel: String,
)

fun toWeightedDwellNoticeView(item: WeightedDwellNotice): WeightedDwellNoticeView {
    return WeightedDwellNoticeView(
        code = item.code,
        titleText = truncate(item.title, 80),
        pallets = item.pallets.toString(),
        capacity = item.capacity.toString(),
        shiftCodeText = truncate(item.shiftCode, 120),
        pinned = "${item.pinned}",
        priority = item.priority.toString(),
        externalRef = truncate(item.externalRef, 40),
        labelText = item.label,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedDwellNoticeViews(items: MutableList<WeightedDwellNotice>): MutableList<WeightedDwellNoticeView> {
    val views = mutableListOf<WeightedDwellNoticeView>()
    for (item in items) {
        views.add(toWeightedDwellNoticeView(item))
    }
    return views
}

fun sumWeightedDwellNoticeView(view: WeightedDwellNoticeView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedPallets.toDouble() + parsedCapacity.toDouble() + parsedPriority.toDouble()
}
