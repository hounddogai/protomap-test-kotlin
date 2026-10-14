package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient near miss claim. */
data class LenientNearMissClaimView(
    var code: String,
    var title: String,
    var checksum: String,
    var label: String,
    var quantity: String,
    var budget: String,
    var discount: String,
    var statusLabel: String,
)

fun toLenientNearMissClaimView(item: LenientNearMissClaim): LenientNearMissClaimView {
    return LenientNearMissClaimView(
        code = truncate(item.code, 64),
        title = item.title,
        checksum = item.checksum,
        label = item.label,
        quantity = item.quantity.toString(),
        budget = "%.2f".format(item.budget),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientNearMissClaimViews(items: MutableList<LenientNearMissClaim>): MutableList<LenientNearMissClaimView> {
    val views = mutableListOf<LenientNearMissClaimView>()
    for (item in items) {
        views.add(toLenientNearMissClaimView(item))
    }
    return views
}

fun sumLenientNearMissClaimView(view: LenientNearMissClaimView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedQuantity.toDouble() + parsedBudget + parsedDiscount
}
