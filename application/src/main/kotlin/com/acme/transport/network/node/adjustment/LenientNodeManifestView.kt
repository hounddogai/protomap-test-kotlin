package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient node manifest. */
data class LenientNodeManifestView(
    var code: String,
    var nameText: String,
    var rate: String,
    var archived: String,
    var score: String,
    var threshold: String,
    var minimum: String,
    var currency: String,
    var statusLabel: String,
)

fun toLenientNodeManifestView(item: LenientNodeManifest): LenientNodeManifestView {
    return LenientNodeManifestView(
        code = item.code,
        nameText = item.name,
        rate = "%.2f".format(item.rate),
        archived = "${item.archived}",
        score = "%.2f".format(item.score),
        threshold = "%.2f".format(item.threshold),
        minimum = "%.2f".format(item.minimum),
        currency = truncate(item.currency, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNodeManifestViews(items: MutableList<LenientNodeManifest>): MutableList<LenientNodeManifestView> {
    val views = mutableListOf<LenientNodeManifestView>()
    for (item in items) {
        views.add(toLenientNodeManifestView(item))
    }
    return views
}

fun sumLenientNodeManifestView(view: LenientNodeManifestView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRate + parsedScore + parsedThreshold
}
