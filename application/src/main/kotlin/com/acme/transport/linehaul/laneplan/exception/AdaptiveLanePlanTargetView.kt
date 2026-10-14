package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive lane plan target. */
data class AdaptiveLanePlanTargetView(
    var code: String,
    var name: String,
    var archived: String,
    var reviewerText: String,
    var category: String,
    var attemptsText: String,
    var windowDays: String,
    var threshold: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toAdaptiveLanePlanTargetView(item: AdaptiveLanePlanTarget): AdaptiveLanePlanTargetView {
    return AdaptiveLanePlanTargetView(
        code = item.code,
        name = item.name,
        archived = "${item.archived}",
        reviewerText = truncate(item.reviewer, 40),
        category = item.category,
        attemptsText = item.attempts.toString(),
        windowDays = item.windowDays.toString(),
        threshold = "%.2f".format(item.threshold),
        trailerId = truncate(item.trailerId, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLanePlanTargetViews(items: MutableList<AdaptiveLanePlanTarget>): MutableList<AdaptiveLanePlanTargetView> {
    val views = mutableListOf<AdaptiveLanePlanTargetView>()
    for (item in items) {
        views.add(toAdaptiveLanePlanTargetView(item))
    }
    return views
}

fun sumAdaptiveLanePlanTargetView(view: AdaptiveLanePlanTargetView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble() + parsedThreshold
}
