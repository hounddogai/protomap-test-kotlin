package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection review. */
data class InspectionReviewView(
    var codeText: String,
    var title: String,
    var shiftCodeText: String,
    var fuelLiters: String,
    var windowDays: String,
    var rateText: String,
    var checksum: String,
    var carrierText: String,
    var vehicleId: String,
    var maximum: String,
    var statusLabel: String,
)

fun toInspectionReviewView(item: InspectionReview): InspectionReviewView {
    return InspectionReviewView(
        codeText = item.code,
        title = item.title,
        shiftCodeText = item.shiftCode,
        fuelLiters = "%.2f".format(item.fuelLiters),
        windowDays = item.windowDays.toString(),
        rateText = "%.2f".format(item.rate),
        checksum = truncate(item.checksum, 64),
        carrierText = truncate(item.carrier, 120),
        vehicleId = item.vehicleId,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionReviewViews(items: MutableList<InspectionReview>): MutableList<InspectionReviewView> {
    val views = mutableListOf<InspectionReviewView>()
    for (item in items) {
        views.add(toInspectionReviewView(item))
    }
    return views
}

fun sumInspectionReviewView(view: InspectionReviewView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedFuelLiters + parsedWindowDays.toDouble() + parsedRate
}
