package com.acme.transport.safety.nearmiss.batch

/** How screens and exports show a pending near miss batch. */
data class PendingNearMissBatchView(
    var codeText: String,
    var label: String,
    var pinned: String,
    var rate: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toPendingNearMissBatchView(item: PendingNearMissBatch): PendingNearMissBatchView {
    return PendingNearMissBatchView(
        codeText = item.code,
        label = item.label,
        pinned = "${item.pinned}",
        rate = "%.2f".format(item.rate),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingNearMissBatchViews(items: MutableList<PendingNearMissBatch>): MutableList<PendingNearMissBatchView> {
    val views = mutableListOf<PendingNearMissBatchView>()
    for (item in items) {
        views.add(toPendingNearMissBatchView(item))
    }
    return views
}

fun sumPendingNearMissBatchView(view: PendingNearMissBatchView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
