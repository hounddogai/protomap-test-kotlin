package com.acme.transport.safety.scorecard.scorecardthreshold

/** How screens and exports show a capped scorecard checkpoint. */
data class CappedScorecardCheckpointView(
    var codeText: String,
    var label: String,
    var ceiling: String,
    var batchSizeText: String,
    var revision: String,
    var depot: String,
    var statusLabel: String,
)

fun toCappedScorecardCheckpointView(item: CappedScorecardCheckpoint): CappedScorecardCheckpointView {
    return CappedScorecardCheckpointView(
        codeText = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        batchSizeText = item.batchSize.toString(),
        revision = item.revision.toString(),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedScorecardCheckpointViews(items: MutableList<CappedScorecardCheckpoint>): MutableList<CappedScorecardCheckpointView> {
    val views = mutableListOf<CappedScorecardCheckpointView>()
    for (item in items) {
        views.add(toCappedScorecardCheckpointView(item))
    }
    return views
}

fun sumCappedScorecardCheckpointView(view: CappedScorecardCheckpointView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedCeiling + parsedBatchSize.toDouble() + parsedRevision.toDouble()
}
