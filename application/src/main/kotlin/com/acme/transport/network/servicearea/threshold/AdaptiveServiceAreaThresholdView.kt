package com.acme.transport.network.servicearea.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive service area threshold. */
data class AdaptiveServiceAreaThresholdView(
    var codeText: String,
    var nameText: String,
    var etaMinutes: String,
    var revisionText: String,
    var carrier: String,
    var ratioText: String,
    var archivedText: String,
    var externalRef: String,
    var visible: String,
    var limit: String,
    var statusLabel: String,
)

fun toAdaptiveServiceAreaThresholdView(item: AdaptiveServiceAreaThreshold): AdaptiveServiceAreaThresholdView {
    return AdaptiveServiceAreaThresholdView(
        codeText = truncate(item.code, 80),
        nameText = item.name,
        etaMinutes = item.etaMinutes.toString(),
        revisionText = item.revision.toString(),
        carrier = item.carrier,
        ratioText = "%.2f".format(item.ratio),
        archivedText = "${item.archived}",
        externalRef = item.externalRef,
        visible = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveServiceAreaThresholdViews(items: MutableList<AdaptiveServiceAreaThreshold>): MutableList<AdaptiveServiceAreaThresholdView> {
    val views = mutableListOf<AdaptiveServiceAreaThresholdView>()
    for (item in items) {
        views.add(toAdaptiveServiceAreaThresholdView(item))
    }
    return views
}

fun sumAdaptiveServiceAreaThresholdView(view: AdaptiveServiceAreaThresholdView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble() + parsedRatio
}
