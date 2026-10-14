package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node milestone. */
data class NodeMilestoneView(
    var code: String,
    var label: String,
    var segment: String,
    var trailerId: String,
    var regionText: String,
    var approved: String,
    var versionText: String,
    var reviewer: String,
    var score: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toNodeMilestoneView(item: NodeMilestone): NodeMilestoneView {
    return NodeMilestoneView(
        code = item.code,
        label = truncate(item.label, 120),
        segment = item.segment,
        trailerId = item.trailerId,
        regionText = item.region,
        approved = "${item.approved}",
        versionText = item.version.toString(),
        reviewer = item.reviewer,
        score = "%.2f".format(item.score),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeMilestoneViews(items: MutableList<NodeMilestone>): MutableList<NodeMilestoneView> {
    val views = mutableListOf<NodeMilestoneView>()
    for (item in items) {
        views.add(toNodeMilestoneView(item))
    }
    return views
}

fun sumNodeMilestoneView(view: NodeMilestoneView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedVersion.toDouble() + parsedScore + parsedThreshold
}
