package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual duty period review. */
data class ManualDutyPeriodReviewView(
    var code: String,
    var titleText: String,
    var discount: String,
    var fuelLiters: String,
    var attempts: String,
    var pallets: String,
    var sequenceText: String,
    var unit: String,
    var statusLabel: String,
)

fun toManualDutyPeriodReviewView(item: ManualDutyPeriodReview): ManualDutyPeriodReviewView {
    return ManualDutyPeriodReviewView(
        code = item.code,
        titleText = truncate(item.title, 40),
        discount = "%.2f".format(item.discount),
        fuelLiters = "%.2f".format(item.fuelLiters),
        attempts = item.attempts.toString(),
        pallets = item.pallets.toString(),
        sequenceText = item.sequence.toString(),
        unit = truncate(item.unit, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDutyPeriodReviewViews(items: MutableList<ManualDutyPeriodReview>): MutableList<ManualDutyPeriodReviewView> {
    val views = mutableListOf<ManualDutyPeriodReviewView>()
    for (item in items) {
        views.add(toManualDutyPeriodReviewView(item))
    }
    return views
}

fun sumManualDutyPeriodReviewView(view: ManualDutyPeriodReviewView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedDiscount + parsedFuelLiters + parsedAttempts.toDouble()
}
