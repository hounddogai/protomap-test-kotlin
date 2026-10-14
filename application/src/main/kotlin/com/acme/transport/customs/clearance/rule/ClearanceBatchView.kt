package com.acme.transport.customs.clearance.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a clearance batch. */
data class ClearanceBatchView(
    var code: String,
    var name: String,
    var driverId: String,
    var pallets: String,
    var phone: String,
    var visibleText: String,
    var segmentText: String,
    var minimum: String,
    var revisionText: String,
    var channelText: String,
    var statusLabel: String,
)

fun toClearanceBatchView(item: ClearanceBatch): ClearanceBatchView {
    return ClearanceBatchView(
        code = item.code,
        name = truncate(item.name, 40),
        driverId = truncate(item.driverId, 40),
        pallets = item.pallets.toString(),
        phone = item.phone,
        visibleText = "${item.visible}",
        segmentText = truncate(item.segment, 40),
        minimum = "%.2f".format(item.minimum),
        revisionText = item.revision.toString(),
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceBatchViews(items: MutableList<ClearanceBatch>): MutableList<ClearanceBatchView> {
    val views = mutableListOf<ClearanceBatchView>()
    for (item in items) {
        views.add(toClearanceBatchView(item))
    }
    return views
}

fun sumClearanceBatchView(view: ClearanceBatchView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedPallets.toDouble() + parsedMinimum + parsedRevision.toDouble()
}
