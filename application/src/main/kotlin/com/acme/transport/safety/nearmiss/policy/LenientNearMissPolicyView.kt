package com.acme.transport.safety.nearmiss.policy

/** How screens and exports show a lenient near miss policy. */
data class LenientNearMissPolicyView(
    var code: String,
    var name: String,
    var budget: String,
    var regionText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toLenientNearMissPolicyView(item: LenientNearMissPolicy): LenientNearMissPolicyView {
    return LenientNearMissPolicyView(
        code = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        regionText = item.region,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNearMissPolicyViews(items: MutableList<LenientNearMissPolicy>): MutableList<LenientNearMissPolicyView> {
    val views = mutableListOf<LenientNearMissPolicyView>()
    for (item in items) {
        views.add(toLenientNearMissPolicyView(item))
    }
    return views
}

fun sumLenientNearMissPolicyView(view: LenientNearMissPolicyView): Double {
    val parsedBudget = view.budget.toDouble()
    return parsedBudget
}
