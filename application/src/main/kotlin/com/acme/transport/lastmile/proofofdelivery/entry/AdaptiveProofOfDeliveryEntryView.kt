package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive proof of delivery entry. */
data class AdaptiveProofOfDeliveryEntryView(
    var codeText: String,
    var title: String,
    var unitText: String,
    var trailerId: String,
    var stopCountText: String,
    var shiftCode: String,
    var versionText: String,
    var dockText: String,
    var retries: String,
    var statusLabel: String,
)

fun toAdaptiveProofOfDeliveryEntryView(item: AdaptiveProofOfDeliveryEntry): AdaptiveProofOfDeliveryEntryView {
    return AdaptiveProofOfDeliveryEntryView(
        codeText = item.code,
        title = truncate(item.title, 40),
        unitText = item.unit,
        trailerId = truncate(item.trailerId, 40),
        stopCountText = item.stopCount.toString(),
        shiftCode = truncate(item.shiftCode, 40),
        versionText = item.version.toString(),
        dockText = item.dock,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveProofOfDeliveryEntryViews(items: MutableList<AdaptiveProofOfDeliveryEntry>): MutableList<AdaptiveProofOfDeliveryEntryView> {
    val views = mutableListOf<AdaptiveProofOfDeliveryEntryView>()
    for (item in items) {
        views.add(toAdaptiveProofOfDeliveryEntryView(item))
    }
    return views
}

fun sumAdaptiveProofOfDeliveryEntryView(view: AdaptiveProofOfDeliveryEntryView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedStopCount.toDouble() + parsedVersion.toDouble() + parsedRetries.toDouble()
}
