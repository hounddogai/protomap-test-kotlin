package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled locker review. */
data class ScheduledLockerReviewView(
    var code: String,
    var name: String,
    var descriptionText: String,
    var amountText: String,
    var phone: String,
    var checksumText: String,
    var sequenceText: String,
    var driverIdText: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toScheduledLockerReviewView(item: ScheduledLockerReview): ScheduledLockerReviewView {
    return ScheduledLockerReviewView(
        code = truncate(item.code, 64),
        name = item.name,
        descriptionText = item.description,
        amountText = "%.2f".format(item.amount),
        phone = truncate(item.phone, 40),
        checksumText = item.checksum,
        sequenceText = item.sequence.toString(),
        driverIdText = truncate(item.driverId, 64),
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLockerReviewViews(items: MutableList<ScheduledLockerReview>): MutableList<ScheduledLockerReviewView> {
    val views = mutableListOf<ScheduledLockerReviewView>()
    for (item in items) {
        views.add(toScheduledLockerReviewView(item))
    }
    return views
}

fun sumScheduledLockerReviewView(view: ScheduledLockerReviewView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedSequence = view.sequenceText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedAmount + parsedSequence.toDouble() + parsedDurationMinutes.toDouble()
}
