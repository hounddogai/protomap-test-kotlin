package com.acme.transport.terminal.sortation.digest

/** How screens and exports show a scheduled sortation ledger. */
data class ScheduledSortationLedgerView(
    var code: String,
    var label: String,
    var phoneText: String,
    var approved: String,
    var locale: String,
    var amountText: String,
    var statusLabel: String,
)

fun toScheduledSortationLedgerView(item: ScheduledSortationLedger): ScheduledSortationLedgerView {
    return ScheduledSortationLedgerView(
        code = item.code,
        label = item.label,
        phoneText = item.phone,
        approved = "${item.approved}",
        locale = item.locale,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledSortationLedgerViews(items: MutableList<ScheduledSortationLedger>): MutableList<ScheduledSortationLedgerView> {
    val views = mutableListOf<ScheduledSortationLedgerView>()
    for (item in items) {
        views.add(toScheduledSortationLedgerView(item))
    }
    return views
}

fun sumScheduledSortationLedgerView(view: ScheduledSortationLedgerView): Double {
    val parsedAmount = view.amountText.toDouble()
    return parsedAmount
}
