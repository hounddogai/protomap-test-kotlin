package com.acme.transport.terminal.sortation.window

/** How screens and exports show a default sortation review. */
data class DefaultSortationReviewView(
    var code: String,
    var label: String,
    var weight: String,
    var depotText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toDefaultSortationReviewView(item: DefaultSortationReview): DefaultSortationReviewView {
    return DefaultSortationReviewView(
        code = item.code,
        label = item.label,
        weight = "%.2f".format(item.weight),
        depotText = item.depot,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultSortationReviewViews(items: MutableList<DefaultSortationReview>): MutableList<DefaultSortationReviewView> {
    val views = mutableListOf<DefaultSortationReviewView>()
    for (item in items) {
        views.add(toDefaultSortationReviewView(item))
    }
    return views
}

fun sumDefaultSortationReviewView(view: DefaultSortationReviewView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedWeight + parsedCreatedAt.toDouble()
}
