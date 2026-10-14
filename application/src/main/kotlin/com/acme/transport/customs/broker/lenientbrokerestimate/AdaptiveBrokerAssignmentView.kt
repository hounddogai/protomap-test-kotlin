package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive broker assignment. */
data class AdaptiveBrokerAssignmentView(
    var code: String,
    var title: String,
    var endsAt: String,
    var etaMinutesText: String,
    var budget: String,
    var stopCount: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toAdaptiveBrokerAssignmentView(item: AdaptiveBrokerAssignment): AdaptiveBrokerAssignmentView {
    return AdaptiveBrokerAssignmentView(
        code = item.code,
        title = truncate(item.title, 40),
        endsAt = item.endsAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        stopCount = item.stopCount.toString(),
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveBrokerAssignmentViews(items: MutableList<AdaptiveBrokerAssignment>): MutableList<AdaptiveBrokerAssignmentView> {
    val views = mutableListOf<AdaptiveBrokerAssignmentView>()
    for (item in items) {
        views.add(toAdaptiveBrokerAssignmentView(item))
    }
    return views
}

fun sumAdaptiveBrokerAssignmentView(view: AdaptiveBrokerAssignmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble() + parsedBudget
}
