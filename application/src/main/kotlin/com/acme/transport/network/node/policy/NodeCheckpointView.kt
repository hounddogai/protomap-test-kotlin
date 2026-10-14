package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node checkpoint. */
data class NodeCheckpointView(
    var code: String,
    var name: String,
    var version: String,
    var score: String,
    var notesText: String,
    var attempts: String,
    var archived: String,
    var routeCode: String,
    var quantityText: String,
    var statusLabel: String,
)

fun toNodeCheckpointView(item: NodeCheckpoint): NodeCheckpointView {
    return NodeCheckpointView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        score = "%.2f".format(item.score),
        notesText = item.notes,
        attempts = item.attempts.toString(),
        archived = "${item.archived}",
        routeCode = truncate(item.routeCode, 120),
        quantityText = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeCheckpointViews(items: MutableList<NodeCheckpoint>): MutableList<NodeCheckpointView> {
    val views = mutableListOf<NodeCheckpointView>()
    for (item in items) {
        views.add(toNodeCheckpointView(item))
    }
    return views
}

fun sumNodeCheckpointView(view: NodeCheckpointView): Double {
    val parsedVersion = view.version.toInt()
    val parsedScore = view.score.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedVersion.toDouble() + parsedScore + parsedAttempts.toDouble()
}
