package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rest break allocation. */
data class RestBreakAllocationView(
    var code: String,
    var title: String,
    var active: String,
    var maximumText: String,
    var pallets: String,
    var shiftCodeText: String,
    var graceDays: String,
    var rate: String,
    var name: String,
    var statusLabel: String,
)

fun toRestBreakAllocationView(item: RestBreakAllocation): RestBreakAllocationView {
    return RestBreakAllocationView(
        code = item.code,
        title = item.title,
        active = "${item.active}",
        maximumText = "%.2f".format(item.maximum),
        pallets = item.pallets.toString(),
        shiftCodeText = item.shiftCode,
        graceDays = item.graceDays.toString(),
        rate = "%.2f".format(item.rate),
        name = truncate(item.name, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRestBreakAllocationViews(items: MutableList<RestBreakAllocation>): MutableList<RestBreakAllocationView> {
    val views = mutableListOf<RestBreakAllocationView>()
    for (item in items) {
        views.add(toRestBreakAllocationView(item))
    }
    return views
}

fun sumRestBreakAllocationView(view: RestBreakAllocationView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedPallets = view.pallets.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMaximum + parsedPallets.toDouble() + parsedGraceDays.toDouble()
}
