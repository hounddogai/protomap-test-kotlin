package com.acme.transport.linehaul.trailer.cycle

/** How screens and exports show a primary trailer claim. */
data class PrimaryTrailerClaimView(
    var code: String,
    var labelText: String,
    var quantityText: String,
    var margin: String,
    var budgetText: String,
    var threshold: String,
    var revision: String,
    var reviewer: String,
    var maximumText: String,
    var discount: String,
    var segment: String,
    var statusLabel: String,
)

fun toPrimaryTrailerClaimView(item: PrimaryTrailerClaim): PrimaryTrailerClaimView {
    return PrimaryTrailerClaimView(
        code = item.code,
        labelText = item.label,
        quantityText = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        budgetText = "%.2f".format(item.budget),
        threshold = "%.2f".format(item.threshold),
        revision = item.revision.toString(),
        reviewer = item.reviewer,
        maximumText = "%.2f".format(item.maximum),
        discount = "%.2f".format(item.discount),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTrailerClaimViews(items: MutableList<PrimaryTrailerClaim>): MutableList<PrimaryTrailerClaimView> {
    val views = mutableListOf<PrimaryTrailerClaimView>()
    for (item in items) {
        views.add(toPrimaryTrailerClaimView(item))
    }
    return views
}

fun sumPrimaryTrailerClaimView(view: PrimaryTrailerClaimView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedQuantity.toDouble() + parsedMargin + parsedBudget
}
