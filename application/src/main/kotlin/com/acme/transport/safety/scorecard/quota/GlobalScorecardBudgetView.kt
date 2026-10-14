package com.acme.transport.safety.scorecard.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global scorecard budget. */
data class GlobalScorecardBudgetView(
    var codeText: String,
    var name: String,
    var revisionText: String,
    var reviewer: String,
    var locale: String,
    var channelText: String,
    var amount: String,
    var approved: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toGlobalScorecardBudgetView(item: GlobalScorecardBudget): GlobalScorecardBudgetView {
    return GlobalScorecardBudgetView(
        codeText = item.code,
        name = item.name,
        revisionText = item.revision.toString(),
        reviewer = item.reviewer,
        locale = truncate(item.locale, 120),
        channelText = item.channel,
        amount = "%.2f".format(item.amount),
        approved = "${item.approved}",
        trailerId = truncate(item.trailerId, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalScorecardBudgetViews(items: MutableList<GlobalScorecardBudget>): MutableList<GlobalScorecardBudgetView> {
    val views = mutableListOf<GlobalScorecardBudgetView>()
    for (item in items) {
        views.add(toGlobalScorecardBudgetView(item))
    }
    return views
}

fun sumGlobalScorecardBudgetView(view: GlobalScorecardBudgetView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedRevision.toDouble() + parsedAmount
}
