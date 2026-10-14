package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell review. */
data class DwellReviewView(
    var code: String,
    var titleText: String,
    var priorityText: String,
    var phone: String,
    var pallets: String,
    var carrierText: String,
    var endsAt: String,
    var statusLabel: String,
)

fun toDwellReviewView(item: DwellReview): DwellReviewView {
    return DwellReviewView(
        code = item.code,
        titleText = truncate(item.title, 64),
        priorityText = item.priority.toString(),
        phone = item.phone,
        pallets = item.pallets.toString(),
        carrierText = item.carrier,
        endsAt = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellReviewViews(items: MutableList<DwellReview>): MutableList<DwellReviewView> {
    val views = mutableListOf<DwellReviewView>()
    for (item in items) {
        views.add(toDwellReviewView(item))
    }
    return views
}

fun sumDwellReviewView(view: DwellReviewView): Double {
    val parsedPriority = view.priorityText.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedPriority.toDouble() + parsedPallets.toDouble() + parsedEndsAt.toDouble()
}
