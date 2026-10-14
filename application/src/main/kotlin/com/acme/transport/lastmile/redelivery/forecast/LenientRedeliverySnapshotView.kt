package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient redelivery snapshot. */
data class LenientRedeliverySnapshotView(
    var codeText: String,
    var label: String,
    var etaMinutesText: String,
    var loadKgText: String,
    var version: String,
    var reviewer: String,
    var capacity: String,
    var statusLabel: String,
)

fun toLenientRedeliverySnapshotView(item: LenientRedeliverySnapshot): LenientRedeliverySnapshotView {
    return LenientRedeliverySnapshotView(
        codeText = item.code,
        label = item.label,
        etaMinutesText = item.etaMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        version = item.version.toString(),
        reviewer = truncate(item.reviewer, 80),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRedeliverySnapshotViews(items: MutableList<LenientRedeliverySnapshot>): MutableList<LenientRedeliverySnapshotView> {
    val views = mutableListOf<LenientRedeliverySnapshotView>()
    for (item in items) {
        views.add(toLenientRedeliverySnapshotView(item))
    }
    return views
}

fun sumLenientRedeliverySnapshotView(view: LenientRedeliverySnapshotView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedEtaMinutes.toDouble() + parsedLoadKg + parsedVersion.toDouble()
}
