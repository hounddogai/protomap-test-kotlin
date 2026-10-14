package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global availability proposal. */
data class GlobalAvailabilityProposalView(
    var code: String,
    var label: String,
    var windowDaysText: String,
    var retries: String,
    var graceDaysText: String,
    var budgetText: String,
    var floorValueText: String,
    var attempts: String,
    var channel: String,
    var statusLabel: String,
)

fun toGlobalAvailabilityProposalView(item: GlobalAvailabilityProposal): GlobalAvailabilityProposalView {
    return GlobalAvailabilityProposalView(
        code = item.code,
        label = truncate(item.label, 80),
        windowDaysText = item.windowDays.toString(),
        retries = item.retries.toString(),
        graceDaysText = item.graceDays.toString(),
        budgetText = "%.2f".format(item.budget),
        floorValueText = "%.2f".format(item.floorValue),
        attempts = item.attempts.toString(),
        channel = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAvailabilityProposalViews(items: MutableList<GlobalAvailabilityProposal>): MutableList<GlobalAvailabilityProposalView> {
    val views = mutableListOf<GlobalAvailabilityProposalView>()
    for (item in items) {
        views.add(toGlobalAvailabilityProposalView(item))
    }
    return views
}

fun sumGlobalAvailabilityProposalView(view: GlobalAvailabilityProposalView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedWindowDays.toDouble() + parsedRetries.toDouble() + parsedGraceDays.toDouble()
}
