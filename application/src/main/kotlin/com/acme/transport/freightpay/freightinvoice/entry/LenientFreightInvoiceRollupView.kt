package com.acme.transport.freightpay.freightinvoice.entry

/** How screens and exports show a lenient freight invoice rollup. */
data class LenientFreightInvoiceRollupView(
    var codeText: String,
    var labelText: String,
    var shiftCodeText: String,
    var graceDays: String,
    var budgetText: String,
    var statusLabel: String,
)

fun toLenientFreightInvoiceRollupView(item: LenientFreightInvoiceRollup): LenientFreightInvoiceRollupView {
    return LenientFreightInvoiceRollupView(
        codeText = item.code,
        labelText = item.label,
        shiftCodeText = item.shiftCode,
        graceDays = item.graceDays.toString(),
        budgetText = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientFreightInvoiceRollupViews(items: MutableList<LenientFreightInvoiceRollup>): MutableList<LenientFreightInvoiceRollupView> {
    val views = mutableListOf<LenientFreightInvoiceRollupView>()
    for (item in items) {
        views.add(toLenientFreightInvoiceRollupView(item))
    }
    return views
}

fun sumLenientFreightInvoiceRollupView(view: LenientFreightInvoiceRollupView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedBudget = view.budgetText.toDouble()
    return parsedGraceDays.toDouble() + parsedBudget
}
