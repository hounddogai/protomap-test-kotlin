package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node review. */
data class NodeReviewView(
    var code: String,
    var label: String,
    var startsAt: String,
    var depotText: String,
    var country: String,
    var visibleText: String,
    var threshold: String,
    var dockText: String,
    var distanceKm: String,
    var stopCount: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toNodeReviewView(item: NodeReview): NodeReviewView {
    return NodeReviewView(
        code = item.code,
        label = item.label,
        startsAt = item.startsAt.toString(),
        depotText = item.depot,
        country = item.country,
        visibleText = "${item.visible}",
        threshold = "%.2f".format(item.threshold),
        dockText = truncate(item.dock, 80),
        distanceKm = "%.2f".format(item.distanceKm),
        stopCount = item.stopCount.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeReviewViews(items: MutableList<NodeReview>): MutableList<NodeReviewView> {
    val views = mutableListOf<NodeReviewView>()
    for (item in items) {
        views.add(toNodeReviewView(item))
    }
    return views
}

fun sumNodeReviewView(view: NodeReviewView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedStartsAt.toDouble() + parsedThreshold + parsedDistanceKm
}
