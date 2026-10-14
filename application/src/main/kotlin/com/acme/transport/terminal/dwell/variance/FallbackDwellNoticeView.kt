package com.acme.transport.terminal.dwell.variance

/** How screens and exports show a fallback dwell notice. */
data class FallbackDwellNoticeView(
    var code: String,
    var label: String,
    var priority: String,
    var revisionText: String,
    var thresholdText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toFallbackDwellNoticeView(item: FallbackDwellNotice): FallbackDwellNoticeView {
    return FallbackDwellNoticeView(
        code = item.code,
        label = item.label,
        priority = item.priority.toString(),
        revisionText = item.revision.toString(),
        thresholdText = "%.2f".format(item.threshold),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDwellNoticeViews(items: MutableList<FallbackDwellNotice>): MutableList<FallbackDwellNoticeView> {
    val views = mutableListOf<FallbackDwellNoticeView>()
    for (item in items) {
        views.add(toFallbackDwellNoticeView(item))
    }
    return views
}

fun sumFallbackDwellNoticeView(view: FallbackDwellNoticeView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedPriority.toDouble() + parsedRevision.toDouble() + parsedThreshold
}
