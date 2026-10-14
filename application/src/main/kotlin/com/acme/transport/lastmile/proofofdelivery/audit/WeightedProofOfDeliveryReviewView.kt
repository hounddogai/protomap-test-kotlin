package com.acme.transport.lastmile.proofofdelivery.audit

/** How screens and exports show a weighted proof of delivery review. */
data class WeightedProofOfDeliveryReviewView(
    var code: String,
    var label: String,
    var odometer: String,
    var durationMinutes: String,
    var loadKgText: String,
    var unitText: String,
    var notesText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toWeightedProofOfDeliveryReviewView(item: WeightedProofOfDeliveryReview): WeightedProofOfDeliveryReviewView {
    return WeightedProofOfDeliveryReviewView(
        code = item.code,
        label = item.label,
        odometer = item.odometer.toString(),
        durationMinutes = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        unitText = item.unit,
        notesText = item.notes,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedProofOfDeliveryReviewViews(items: MutableList<WeightedProofOfDeliveryReview>): MutableList<WeightedProofOfDeliveryReviewView> {
    val views = mutableListOf<WeightedProofOfDeliveryReviewView>()
    for (item in items) {
        views.add(toWeightedProofOfDeliveryReviewView(item))
    }
    return views
}

fun sumWeightedProofOfDeliveryReviewView(view: WeightedProofOfDeliveryReviewView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedOdometer.toDouble() + parsedDurationMinutes.toDouble() + parsedLoadKg
}
