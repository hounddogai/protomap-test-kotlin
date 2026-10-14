package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard agreement. */
data class ScorecardAgreementView(
    var codeText: String,
    var nameText: String,
    var amount: String,
    var cost: String,
    var unit: String,
    var statusLabel: String,
)

fun toScorecardAgreementView(item: ScorecardAgreement): ScorecardAgreementView {
    return ScorecardAgreementView(
        codeText = item.code,
        nameText = truncate(item.name, 64),
        amount = "%.2f".format(item.amount),
        cost = "%.2f".format(item.cost),
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardAgreementViews(items: MutableList<ScorecardAgreement>): MutableList<ScorecardAgreementView> {
    val views = mutableListOf<ScorecardAgreementView>()
    for (item in items) {
        views.add(toScorecardAgreementView(item))
    }
    return views
}

fun sumScorecardAgreementView(view: ScorecardAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedAmount + parsedCost
}
