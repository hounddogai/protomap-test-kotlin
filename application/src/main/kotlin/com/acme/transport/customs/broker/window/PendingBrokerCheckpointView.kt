package com.acme.transport.customs.broker.window

/** How screens and exports show a pending broker checkpoint. */
data class PendingBrokerCheckpointView(
    var codeText: String,
    var name: String,
    var ceiling: String,
    var externalRef: String,
    var summary: String,
    var graceDays: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toPendingBrokerCheckpointView(item: PendingBrokerCheckpoint): PendingBrokerCheckpointView {
    return PendingBrokerCheckpointView(
        codeText = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        summary = item.summary,
        graceDays = item.graceDays.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingBrokerCheckpointViews(items: MutableList<PendingBrokerCheckpoint>): MutableList<PendingBrokerCheckpointView> {
    val views = mutableListOf<PendingBrokerCheckpointView>()
    for (item in items) {
        views.add(toPendingBrokerCheckpointView(item))
    }
    return views
}

fun sumPendingBrokerCheckpointView(view: PendingBrokerCheckpointView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedCeiling + parsedGraceDays.toDouble() + parsedThreshold
}
