package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal freight invoice rule. */
data class SeasonalFreightInvoiceRuleView(
    var code: String,
    var name: String,
    var budgetText: String,
    var discount: String,
    var amount: String,
    var notes: String,
    var shiftCode: String,
    var cost: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toSeasonalFreightInvoiceRuleView(item: SeasonalFreightInvoiceRule): SeasonalFreightInvoiceRuleView {
    return SeasonalFreightInvoiceRuleView(
        code = item.code,
        name = truncate(item.name, 64),
        budgetText = "%.2f".format(item.budget),
        discount = "%.2f".format(item.discount),
        amount = "%.2f".format(item.amount),
        notes = truncate(item.notes, 80),
        shiftCode = item.shiftCode,
        cost = "%.2f".format(item.cost),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFreightInvoiceRuleViews(items: MutableList<SeasonalFreightInvoiceRule>): MutableList<SeasonalFreightInvoiceRuleView> {
    val views = mutableListOf<SeasonalFreightInvoiceRuleView>()
    for (item in items) {
        views.add(toSeasonalFreightInvoiceRuleView(item))
    }
    return views
}

fun sumSeasonalFreightInvoiceRuleView(view: SeasonalFreightInvoiceRuleView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedAmount = view.amount.toDouble()
    return parsedBudget + parsedDiscount + parsedAmount
}
