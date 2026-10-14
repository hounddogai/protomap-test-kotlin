package com.acme.transport.network.node.weightednodeagreement

/** How screens and exports show a node record. */
data class NodeRecordView(
    var code: String,
    var nameText: String,
    var capacity: String,
    var vehicleId: String,
    var batchSize: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toNodeRecordView(item: NodeRecord): NodeRecordView {
    return NodeRecordView(
        code = item.code,
        nameText = item.name,
        capacity = item.capacity.toString(),
        vehicleId = item.vehicleId,
        batchSize = item.batchSize.toString(),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeRecordViews(items: MutableList<NodeRecord>): MutableList<NodeRecordView> {
    val views = mutableListOf<NodeRecordView>()
    for (item in items) {
        views.add(toNodeRecordView(item))
    }
    return views
}

fun sumNodeRecordView(view: NodeRecordView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedCapacity.toDouble() + parsedBatchSize.toDouble() + parsedEtaMinutes.toDouble()
}
