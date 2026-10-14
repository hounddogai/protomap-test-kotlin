package com.acme.transport.network.zone.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global zone review. */
data class GlobalZoneReviewView(
    var code: String,
    var title: String,
    var externalRef: String,
    var quantityText: String,
    var country: String,
    var loadKg: String,
    var durationMinutes: String,
    var vehicleId: String,
    var shiftCode: String,
    var approved: String,
    var statusLabel: String,
)

fun toGlobalZoneReviewView(item: GlobalZoneReview): GlobalZoneReviewView {
    return GlobalZoneReviewView(
        code = item.code,
        title = item.title,
        externalRef = item.externalRef,
        quantityText = item.quantity.toString(),
        country = item.country,
        loadKg = "%.2f".format(item.loadKg),
        durationMinutes = item.durationMinutes.toString(),
        vehicleId = truncate(item.vehicleId, 80),
        shiftCode = truncate(item.shiftCode, 80),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalZoneReviewViews(items: MutableList<GlobalZoneReview>): MutableList<GlobalZoneReviewView> {
    val views = mutableListOf<GlobalZoneReviewView>()
    for (item in items) {
        views.add(toGlobalZoneReviewView(item))
    }
    return views
}

fun sumGlobalZoneReviewView(view: GlobalZoneReviewView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedQuantity.toDouble() + parsedLoadKg + parsedDurationMinutes.toDouble()
}
