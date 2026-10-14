package com.acme.transport.lastmile.attempt.baseline

/** How screens and exports show a global attempt review. */
data class GlobalAttemptReviewView(
    var code: String,
    var nameText: String,
    var routeCodeText: String,
    var endsAt: String,
    var category: String,
    var statusLabel: String,
)

fun toGlobalAttemptReviewView(item: GlobalAttemptReview): GlobalAttemptReviewView {
    return GlobalAttemptReviewView(
        code = item.code,
        nameText = item.name,
        routeCodeText = item.routeCode,
        endsAt = item.endsAt.toString(),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAttemptReviewViews(items: MutableList<GlobalAttemptReview>): MutableList<GlobalAttemptReviewView> {
    val views = mutableListOf<GlobalAttemptReviewView>()
    for (item in items) {
        views.add(toGlobalAttemptReviewView(item))
    }
    return views
}

fun sumGlobalAttemptReviewView(view: GlobalAttemptReviewView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
