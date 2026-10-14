package com.acme.transport.fleetops.defect.proposal

/** How screens and exports show a defect score. */
data class DefectScoreView(
    var codeText: String,
    var label: String,
    var priorityText: String,
    var ratio: String,
    var sequenceText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toDefectScoreView(item: DefectScore): DefectScoreView {
    return DefectScoreView(
        codeText = item.code,
        label = item.label,
        priorityText = item.priority.toString(),
        ratio = "%.2f".format(item.ratio),
        sequenceText = item.sequence.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefectScoreViews(items: MutableList<DefectScore>): MutableList<DefectScoreView> {
    val views = mutableListOf<DefectScoreView>()
    for (item in items) {
        views.add(toDefectScoreView(item))
    }
    return views
}

fun sumDefectScoreView(view: DefectScoreView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    return parsedPriority.toDouble() + parsedRatio + parsedSequence.toDouble()
}
