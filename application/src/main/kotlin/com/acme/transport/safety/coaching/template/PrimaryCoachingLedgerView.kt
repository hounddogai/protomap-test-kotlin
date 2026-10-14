package com.acme.transport.safety.coaching.template

/** How screens and exports show a primary coaching ledger. */
data class PrimaryCoachingLedgerView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var trailerId: String,
    var discount: String,
    var labelText: String,
    var reviewer: String,
    var timezone: String,
    var statusLabel: String,
)

fun toPrimaryCoachingLedgerView(item: PrimaryCoachingLedger): PrimaryCoachingLedgerView {
    return PrimaryCoachingLedgerView(
        code = item.code,
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        trailerId = item.trailerId,
        discount = "%.2f".format(item.discount),
        labelText = item.label,
        reviewer = item.reviewer,
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryCoachingLedgerViews(items: MutableList<PrimaryCoachingLedger>): MutableList<PrimaryCoachingLedgerView> {
    val views = mutableListOf<PrimaryCoachingLedgerView>()
    for (item in items) {
        views.add(toPrimaryCoachingLedgerView(item))
    }
    return views
}

fun sumPrimaryCoachingLedgerView(view: PrimaryCoachingLedgerView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedDiscount
}
