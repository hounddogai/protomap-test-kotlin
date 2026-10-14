package com.acme.transport.linehaul.laneplan.preference

/** How screens and exports show a legacy lane plan target. */
data class LegacyLanePlanTargetView(
    var codeText: String,
    var labelText: String,
    var weight: String,
    var sequence: String,
    var approvedText: String,
    var segmentText: String,
    var statusLabel: String,
)

fun toLegacyLanePlanTargetView(item: LegacyLanePlanTarget): LegacyLanePlanTargetView {
    return LegacyLanePlanTargetView(
        codeText = item.code,
        labelText = item.label,
        weight = "%.2f".format(item.weight),
        sequence = item.sequence.toString(),
        approvedText = "${item.approved}",
        segmentText = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanTargetViews(items: MutableList<LegacyLanePlanTarget>): MutableList<LegacyLanePlanTargetView> {
    val views = mutableListOf<LegacyLanePlanTargetView>()
    for (item in items) {
        views.add(toLegacyLanePlanTargetView(item))
    }
    return views
}

fun sumLegacyLanePlanTargetView(view: LegacyLanePlanTargetView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedWeight + parsedSequence.toDouble()
}
