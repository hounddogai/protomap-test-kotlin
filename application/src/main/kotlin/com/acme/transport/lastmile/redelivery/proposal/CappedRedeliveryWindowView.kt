package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped redelivery window. */
data class CappedRedeliveryWindowView(
    var codeText: String,
    var label: String,
    var checksumText: String,
    var contactEmail: String,
    var stopCountText: String,
    var visible: String,
    var notes: String,
    var ownerId: String,
    var dockText: String,
    var threshold: String,
    var description: String,
    var statusLabel: String,
)

fun toCappedRedeliveryWindowView(item: CappedRedeliveryWindow): CappedRedeliveryWindowView {
    return CappedRedeliveryWindowView(
        codeText = item.code,
        label = item.label,
        checksumText = item.checksum,
        contactEmail = item.contactEmail,
        stopCountText = item.stopCount.toString(),
        visible = "${item.visible}",
        notes = item.notes,
        ownerId = truncate(item.ownerId, 120),
        dockText = item.dock,
        threshold = "%.2f".format(item.threshold),
        description = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedRedeliveryWindowViews(items: MutableList<CappedRedeliveryWindow>): MutableList<CappedRedeliveryWindowView> {
    val views = mutableListOf<CappedRedeliveryWindowView>()
    for (item in items) {
        views.add(toCappedRedeliveryWindowView(item))
    }
    return views
}

fun sumCappedRedeliveryWindowView(view: CappedRedeliveryWindowView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedStopCount.toDouble() + parsedThreshold
}
