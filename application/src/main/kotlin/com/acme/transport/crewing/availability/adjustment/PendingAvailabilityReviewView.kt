package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending availability review. */
data class PendingAvailabilityReviewView(
    var code: String,
    var label: String,
    var notes: String,
    var amount: String,
    var title: String,
    var capacity: String,
    var windowDaysText: String,
    var odometer: String,
    var margin: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toPendingAvailabilityReviewView(item: PendingAvailabilityReview): PendingAvailabilityReviewView {
    return PendingAvailabilityReviewView(
        code = item.code,
        label = truncate(item.label, 64),
        notes = item.notes,
        amount = "%.2f".format(item.amount),
        title = item.title,
        capacity = item.capacity.toString(),
        windowDaysText = item.windowDays.toString(),
        odometer = item.odometer.toString(),
        margin = "%.2f".format(item.margin),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingAvailabilityReviewViews(items: MutableList<PendingAvailabilityReview>): MutableList<PendingAvailabilityReviewView> {
    val views = mutableListOf<PendingAvailabilityReviewView>()
    for (item in items) {
        views.add(toPendingAvailabilityReviewView(item))
    }
    return views
}

fun sumPendingAvailabilityReviewView(view: PendingAvailabilityReviewView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    return parsedAmount + parsedCapacity.toDouble() + parsedWindowDays.toDouble()
}
