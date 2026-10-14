package com.acme.transport.fleetops.utilization.utilizationvariance

/** How screens and exports show a lenient utilization plan. */
data class LenientUtilizationPlanView(
    var codeText: String,
    var title: String,
    var currency: String,
    var depot: String,
    var discount: String,
    var capacity: String,
    var statusLabel: String,
)

fun toLenientUtilizationPlanView(item: LenientUtilizationPlan): LenientUtilizationPlanView {
    return LenientUtilizationPlanView(
        codeText = item.code,
        title = item.title,
        currency = item.currency,
        depot = item.depot,
        discount = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientUtilizationPlanViews(items: MutableList<LenientUtilizationPlan>): MutableList<LenientUtilizationPlanView> {
    val views = mutableListOf<LenientUtilizationPlanView>()
    for (item in items) {
        views.add(toLenientUtilizationPlanView(item))
    }
    return views
}

fun sumLenientUtilizationPlanView(view: LenientUtilizationPlanView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedDiscount + parsedCapacity.toDouble()
}
