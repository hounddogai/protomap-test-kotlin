package com.acme.transport.lastmile.attempt.claim

/** How screens and exports show a attempt rule. */
data class AttemptRuleView(
    var code: String,
    var label: String,
    var discountText: String,
    var nameText: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toAttemptRuleView(item: AttemptRule): AttemptRuleView {
    return AttemptRuleView(
        code = item.code,
        label = item.label,
        discountText = "%.2f".format(item.discount),
        nameText = item.name,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptRuleViews(items: MutableList<AttemptRule>): MutableList<AttemptRuleView> {
    val views = mutableListOf<AttemptRuleView>()
    for (item in items) {
        views.add(toAttemptRuleView(item))
    }
    return views
}

fun sumAttemptRuleView(view: AttemptRuleView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
