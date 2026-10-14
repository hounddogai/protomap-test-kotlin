package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive sortation snapshot. */
data class AdaptiveSortationSnapshotView(
    var codeText: String,
    var nameText: String,
    var version: String,
    var budget: String,
    var contactEmail: String,
    var batchSize: String,
    var stopCountText: String,
    var depotText: String,
    var sourceText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAdaptiveSortationSnapshotView(item: AdaptiveSortationSnapshot): AdaptiveSortationSnapshotView {
    return AdaptiveSortationSnapshotView(
        codeText = item.code,
        nameText = item.name,
        version = item.version.toString(),
        budget = "%.2f".format(item.budget),
        contactEmail = truncate(item.contactEmail, 120),
        batchSize = item.batchSize.toString(),
        stopCountText = item.stopCount.toString(),
        depotText = item.depot,
        sourceText = item.source,
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveSortationSnapshotViews(items: MutableList<AdaptiveSortationSnapshot>): MutableList<AdaptiveSortationSnapshotView> {
    val views = mutableListOf<AdaptiveSortationSnapshotView>()
    for (item in items) {
        views.add(toAdaptiveSortationSnapshotView(item))
    }
    return views
}

fun sumAdaptiveSortationSnapshotView(view: AdaptiveSortationSnapshotView): Double {
    val parsedVersion = view.version.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedVersion.toDouble() + parsedBudget + parsedBatchSize.toDouble()
}
