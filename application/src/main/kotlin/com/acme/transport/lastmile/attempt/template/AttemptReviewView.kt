package com.acme.transport.lastmile.attempt.template

/** How screens and exports show a attempt review. */
data class AttemptReviewView(
    var code: String,
    var nameText: String,
    var ceiling: String,
    var priorityText: String,
    var shiftCode: String,
    var notes: String,
    var statusLabel: String,
)

fun toAttemptReviewView(item: AttemptReview): AttemptReviewView {
    return AttemptReviewView(
        code = item.code,
        nameText = item.name,
        ceiling = "%.2f".format(item.ceiling),
        priorityText = item.priority.toString(),
        shiftCode = item.shiftCode,
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptReviewViews(items: MutableList<AttemptReview>): MutableList<AttemptReviewView> {
    val views = mutableListOf<AttemptReviewView>()
    for (item in items) {
        views.add(toAttemptReviewView(item))
    }
    return views
}

fun sumAttemptReviewView(view: AttemptReviewView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedCeiling + parsedPriority.toDouble()
}
