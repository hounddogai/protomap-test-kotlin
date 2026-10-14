package com.acme.transport.emissions.idling.idlingmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling override. */
data class IdlingOverrideView(
    var codeText: String,
    var titleText: String,
    var carrier: String,
    var segment: String,
    var routeCode: String,
    var label: String,
    var checksum: String,
    var pallets: String,
    var floorValue: String,
    var sequence: String,
    var discount: String,
    var statusLabel: String,
)

fun toIdlingOverrideView(item: IdlingOverride): IdlingOverrideView {
    return IdlingOverrideView(
        codeText = item.code,
        titleText = truncate(item.title, 40),
        carrier = item.carrier,
        segment = item.segment,
        routeCode = item.routeCode,
        label = item.label,
        checksum = item.checksum,
        pallets = item.pallets.toString(),
        floorValue = "%.2f".format(item.floorValue),
        sequence = item.sequence.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingOverrideViews(items: MutableList<IdlingOverride>): MutableList<IdlingOverrideView> {
    val views = mutableListOf<IdlingOverrideView>()
    for (item in items) {
        views.add(toIdlingOverrideView(item))
    }
    return views
}

fun sumIdlingOverrideView(view: IdlingOverrideView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedPallets.toDouble() + parsedFloorValue + parsedSequence.toDouble()
}
