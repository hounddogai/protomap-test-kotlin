package com.acme.transport.fleetops.serviceplan.threshold

/** How screens and exports show a strict service plan claim. */
data class StrictServicePlanClaimView(
    var code: String,
    var labelText: String,
    var pinnedText: String,
    var discountText: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toStrictServicePlanClaimView(item: StrictServicePlanClaim): StrictServicePlanClaimView {
    return StrictServicePlanClaimView(
        code = item.code,
        labelText = item.label,
        pinnedText = "${item.pinned}",
        discountText = "%.2f".format(item.discount),
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictServicePlanClaimViews(items: MutableList<StrictServicePlanClaim>): MutableList<StrictServicePlanClaimView> {
    val views = mutableListOf<StrictServicePlanClaimView>()
    for (item in items) {
        views.add(toStrictServicePlanClaimView(item))
    }
    return views
}

fun sumStrictServicePlanClaimView(view: StrictServicePlanClaimView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
