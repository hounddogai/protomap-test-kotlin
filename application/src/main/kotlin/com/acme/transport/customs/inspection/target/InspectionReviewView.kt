package com.acme.transport.customs.inspection.target

/** How screens and exports show a inspection review. */
data class InspectionReviewView(
    var code: String,
    var name: String,
    var ceilingText: String,
    var channel: String,
    var ownerId: String,
    var minimum: String,
    var expiresAt: String,
    var discountText: String,
    var ratio: String,
    var capacity: String,
    var statusLabel: String,
)

fun toInspectionReviewView(item: InspectionReview): InspectionReviewView {
    return InspectionReviewView(
        code = item.code,
        name = item.name,
        ceilingText = "%.2f".format(item.ceiling),
        channel = item.channel,
        ownerId = item.ownerId,
        minimum = "%.2f".format(item.minimum),
        expiresAt = item.expiresAt.toString(),
        discountText = "%.2f".format(item.discount),
        ratio = "%.2f".format(item.ratio),
        capacity = item.capacity.toString(),
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
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCeiling + parsedMinimum + parsedExpiresAt.toDouble()
}
