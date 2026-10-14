package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel surcharge review. */
data class FuelSurchargeReviewView(
    var code: String,
    var labelText: String,
    var country: String,
    var driverId: String,
    var discount: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toFuelSurchargeReviewView(item: FuelSurchargeReview): FuelSurchargeReviewView {
    return FuelSurchargeReviewView(
        code = truncate(item.code, 120),
        labelText = item.label,
        country = item.country,
        driverId = item.driverId,
        discount = "%.2f".format(item.discount),
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeReviewViews(items: MutableList<FuelSurchargeReview>): MutableList<FuelSurchargeReviewView> {
    val views = mutableListOf<FuelSurchargeReviewView>()
    for (item in items) {
        views.add(toFuelSurchargeReviewView(item))
    }
    return views
}

fun sumFuelSurchargeReviewView(view: FuelSurchargeReviewView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedDiscount + parsedUpdatedAt.toDouble()
}
