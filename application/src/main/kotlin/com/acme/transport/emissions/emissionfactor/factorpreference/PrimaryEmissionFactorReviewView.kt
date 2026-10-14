package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary emission factor review. */
data class PrimaryEmissionFactorReviewView(
    var codeText: String,
    var labelText: String,
    var contactEmailText: String,
    var country: String,
    var depot: String,
    var weightText: String,
    var currency: String,
    var quantity: String,
    var endsAt: String,
    var carrier: String,
    var capacityText: String,
    var statusLabel: String,
)

fun toPrimaryEmissionFactorReviewView(item: PrimaryEmissionFactorReview): PrimaryEmissionFactorReviewView {
    return PrimaryEmissionFactorReviewView(
        codeText = item.code,
        labelText = item.label,
        contactEmailText = truncate(item.contactEmail, 80),
        country = truncate(item.country, 64),
        depot = item.depot,
        weightText = "%.2f".format(item.weight),
        currency = item.currency,
        quantity = item.quantity.toString(),
        endsAt = item.endsAt.toString(),
        carrier = item.carrier,
        capacityText = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEmissionFactorReviewViews(items: MutableList<PrimaryEmissionFactorReview>): MutableList<PrimaryEmissionFactorReviewView> {
    val views = mutableListOf<PrimaryEmissionFactorReviewView>()
    for (item in items) {
        views.add(toPrimaryEmissionFactorReviewView(item))
    }
    return views
}

fun sumPrimaryEmissionFactorReviewView(view: PrimaryEmissionFactorReviewView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedQuantity = view.quantity.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedWeight + parsedQuantity.toDouble() + parsedEndsAt.toDouble()
}
