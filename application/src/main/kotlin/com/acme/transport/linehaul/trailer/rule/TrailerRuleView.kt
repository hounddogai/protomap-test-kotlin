package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a trailer rule. */
data class TrailerRuleView(
    var codeText: String,
    var label: String,
    var amount: String,
    var sequence: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTrailerRuleView(item: TrailerRule): TrailerRuleView {
    return TrailerRuleView(
        codeText = item.code,
        label = truncate(item.label, 120),
        amount = "%.2f".format(item.amount),
        sequence = item.sequence.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerRuleViews(items: MutableList<TrailerRule>): MutableList<TrailerRuleView> {
    val views = mutableListOf<TrailerRuleView>()
    for (item in items) {
        views.add(toTrailerRuleView(item))
    }
    return views
}

fun sumTrailerRuleView(view: TrailerRuleView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedAmount + parsedSequence.toDouble() + parsedAttempts.toDouble()
}
