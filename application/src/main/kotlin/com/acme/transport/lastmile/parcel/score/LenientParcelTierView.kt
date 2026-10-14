package com.acme.transport.lastmile.parcel.score

/** How screens and exports show a lenient parcel tier. */
data class LenientParcelTierView(
    var code: String,
    var titleText: String,
    var budget: String,
    var discount: String,
    var statusLabel: String,
)

fun toLenientParcelTierView(item: LenientParcelTier): LenientParcelTierView {
    return LenientParcelTierView(
        code = item.code,
        titleText = item.title,
        budget = "%.2f".format(item.budget),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientParcelTierViews(items: MutableList<LenientParcelTier>): MutableList<LenientParcelTierView> {
    val views = mutableListOf<LenientParcelTierView>()
    for (item in items) {
        views.add(toLenientParcelTierView(item))
    }
    return views
}

fun sumLenientParcelTierView(view: LenientParcelTierView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedBudget + parsedDiscount
}
