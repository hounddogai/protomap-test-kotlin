package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling hub milestone. */
data class RollingHubMilestoneView(
    var codeText: String,
    var title: String,
    var expiresAt: String,
    var createdAtText: String,
    var contactEmail: String,
    var loadKgText: String,
    var routeCodeText: String,
    var maximum: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toRollingHubMilestoneView(item: RollingHubMilestone): RollingHubMilestoneView {
    return RollingHubMilestoneView(
        codeText = item.code,
        title = truncate(item.title, 64),
        expiresAt = item.expiresAt.toString(),
        createdAtText = item.createdAt.toString(),
        contactEmail = truncate(item.contactEmail, 64),
        loadKgText = "%.2f".format(item.loadKg),
        routeCodeText = item.routeCode,
        maximum = "%.2f".format(item.maximum),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingHubMilestoneViews(items: MutableList<RollingHubMilestone>): MutableList<RollingHubMilestoneView> {
    val views = mutableListOf<RollingHubMilestoneView>()
    for (item in items) {
        views.add(toRollingHubMilestoneView(item))
    }
    return views
}

fun sumRollingHubMilestoneView(view: RollingHubMilestoneView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedExpiresAt.toDouble() + parsedCreatedAt.toDouble() + parsedLoadKg
}
