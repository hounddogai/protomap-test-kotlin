package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive node review. */
data class AdaptiveNodeReviewView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var notesText: String,
    var budgetText: String,
    var visibleText: String,
    var channel: String,
    var versionText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toAdaptiveNodeReviewView(item: AdaptiveNodeReview): AdaptiveNodeReviewView {
    return AdaptiveNodeReviewView(
        code = truncate(item.code, 80),
        name = item.name,
        endsAtText = item.endsAt.toString(),
        notesText = truncate(item.notes, 64),
        budgetText = "%.2f".format(item.budget),
        visibleText = "${item.visible}",
        channel = item.channel,
        versionText = item.version.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNodeReviewViews(items: MutableList<AdaptiveNodeReview>): MutableList<AdaptiveNodeReviewView> {
    val views = mutableListOf<AdaptiveNodeReviewView>()
    for (item in items) {
        views.add(toAdaptiveNodeReviewView(item))
    }
    return views
}

fun sumAdaptiveNodeReviewView(view: AdaptiveNodeReviewView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedBudget = view.budgetText.toDouble()
    val parsedVersion = view.versionText.toInt()
    return parsedEndsAt.toDouble() + parsedBudget + parsedVersion.toDouble()
}
