package com.acme.transport.network.hub.metric

/** How screens and exports show a primary hub target. */
data class PrimaryHubTargetView(
    var code: String,
    var titleText: String,
    var version: String,
    var country: String,
    var loadKg: String,
    var sequenceText: String,
    var ceiling: String,
    var currency: String,
    var active: String,
    var statusLabel: String,
)

fun toPrimaryHubTargetView(item: PrimaryHubTarget): PrimaryHubTargetView {
    return PrimaryHubTargetView(
        code = item.code,
        titleText = item.title,
        version = item.version.toString(),
        country = item.country,
        loadKg = "%.2f".format(item.loadKg),
        sequenceText = item.sequence.toString(),
        ceiling = "%.2f".format(item.ceiling),
        currency = item.currency,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryHubTargetViews(items: MutableList<PrimaryHubTarget>): MutableList<PrimaryHubTargetView> {
    val views = mutableListOf<PrimaryHubTargetView>()
    for (item in items) {
        views.add(toPrimaryHubTargetView(item))
    }
    return views
}

fun sumPrimaryHubTargetView(view: PrimaryHubTargetView): Double {
    val parsedVersion = view.version.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedVersion.toDouble() + parsedLoadKg + parsedSequence.toDouble()
}
