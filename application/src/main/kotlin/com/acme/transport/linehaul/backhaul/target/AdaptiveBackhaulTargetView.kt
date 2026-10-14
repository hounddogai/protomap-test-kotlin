package com.acme.transport.linehaul.backhaul.target

/** How screens and exports show a adaptive backhaul target. */
data class AdaptiveBackhaulTargetView(
    var codeText: String,
    var label: String,
    var versionText: String,
    var ceiling: String,
    var sequence: String,
    var stopCount: String,
    var revisionText: String,
    var rateText: String,
    var locale: String,
    var statusLabel: String,
)

fun toAdaptiveBackhaulTargetView(item: AdaptiveBackhaulTarget): AdaptiveBackhaulTargetView {
    return AdaptiveBackhaulTargetView(
        codeText = item.code,
        label = item.label,
        versionText = item.version.toString(),
        ceiling = "%.2f".format(item.ceiling),
        sequence = item.sequence.toString(),
        stopCount = item.stopCount.toString(),
        revisionText = item.revision.toString(),
        rateText = "%.2f".format(item.rate),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBackhaulTargetViews(items: MutableList<AdaptiveBackhaulTarget>): MutableList<AdaptiveBackhaulTargetView> {
    val views = mutableListOf<AdaptiveBackhaulTargetView>()
    for (item in items) {
        views.add(toAdaptiveBackhaulTargetView(item))
    }
    return views
}

fun sumAdaptiveBackhaulTargetView(view: AdaptiveBackhaulTargetView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedVersion.toDouble() + parsedCeiling + parsedSequence.toDouble()
}
