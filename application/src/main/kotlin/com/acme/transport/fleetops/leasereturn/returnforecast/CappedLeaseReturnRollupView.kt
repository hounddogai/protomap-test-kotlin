package com.acme.transport.fleetops.leasereturn.returnforecast

/** How screens and exports show a capped lease return rollup. */
data class CappedLeaseReturnRollupView(
    var codeText: String,
    var label: String,
    var rate: String,
    var discountText: String,
    var statusLabel: String,
)

fun toCappedLeaseReturnRollupView(item: CappedLeaseReturnRollup): CappedLeaseReturnRollupView {
    return CappedLeaseReturnRollupView(
        codeText = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLeaseReturnRollupViews(items: MutableList<CappedLeaseReturnRollup>): MutableList<CappedLeaseReturnRollupView> {
    val views = mutableListOf<CappedLeaseReturnRollupView>()
    for (item in items) {
        views.add(toCappedLeaseReturnRollupView(item))
    }
    return views
}

fun sumCappedLeaseReturnRollupView(view: CappedLeaseReturnRollupView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedRate + parsedDiscount
}
