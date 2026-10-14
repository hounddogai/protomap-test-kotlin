package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive emission factor request. */
data class AdaptiveEmissionFactorRequestView(
    var codeText: String,
    var title: String,
    var revisionText: String,
    var limit: String,
    var threshold: String,
    var minimumText: String,
    var scoreText: String,
    var createdAt: String,
    var trailerId: String,
    var active: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toAdaptiveEmissionFactorRequestView(item: AdaptiveEmissionFactorRequest): AdaptiveEmissionFactorRequestView {
    return AdaptiveEmissionFactorRequestView(
        codeText = item.code,
        title = truncate(item.title, 80),
        revisionText = item.revision.toString(),
        limit = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        minimumText = "%.2f".format(item.minimum),
        scoreText = "%.2f".format(item.score),
        createdAt = item.createdAt.toString(),
        trailerId = item.trailerId,
        active = "${item.active}",
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveEmissionFactorRequestViews(items: MutableList<AdaptiveEmissionFactorRequest>): MutableList<AdaptiveEmissionFactorRequestView> {
    val views = mutableListOf<AdaptiveEmissionFactorRequestView>()
    for (item in items) {
        views.add(toAdaptiveEmissionFactorRequestView(item))
    }
    return views
}

fun sumAdaptiveEmissionFactorRequestView(view: AdaptiveEmissionFactorRequestView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRevision.toDouble() + parsedLimit.toDouble() + parsedThreshold
}
