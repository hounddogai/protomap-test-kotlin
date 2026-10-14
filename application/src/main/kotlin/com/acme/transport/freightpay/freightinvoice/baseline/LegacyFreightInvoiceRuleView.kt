package com.acme.transport.freightpay.freightinvoice.baseline

/** How screens and exports show a legacy freight invoice rule. */
data class LegacyFreightInvoiceRuleView(
    var code: String,
    var title: String,
    var limit: String,
    var minimumText: String,
    var cost: String,
    var weight: String,
    var notes: String,
    var name: String,
    var statusLabel: String,
)

fun toLegacyFreightInvoiceRuleView(item: LegacyFreightInvoiceRule): LegacyFreightInvoiceRuleView {
    return LegacyFreightInvoiceRuleView(
        code = item.code,
        title = item.title,
        limit = item.limit.toString(),
        minimumText = "%.2f".format(item.minimum),
        cost = "%.2f".format(item.cost),
        weight = "%.2f".format(item.weight),
        notes = item.notes,
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyFreightInvoiceRuleViews(items: MutableList<LegacyFreightInvoiceRule>): MutableList<LegacyFreightInvoiceRuleView> {
    val views = mutableListOf<LegacyFreightInvoiceRuleView>()
    for (item in items) {
        views.add(toLegacyFreightInvoiceRuleView(item))
    }
    return views
}

fun sumLegacyFreightInvoiceRuleView(view: LegacyFreightInvoiceRuleView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedLimit.toDouble() + parsedMinimum + parsedCost
}
