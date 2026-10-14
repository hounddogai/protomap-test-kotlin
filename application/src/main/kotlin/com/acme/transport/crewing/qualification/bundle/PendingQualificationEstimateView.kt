package com.acme.transport.crewing.qualification.bundle

/** How screens and exports show a pending qualification estimate. */
data class PendingQualificationEstimateView(
    var code: String,
    var label: String,
    var quantityText: String,
    var minimum: String,
    var discountText: String,
    var statusLabel: String,
)

fun toPendingQualificationEstimateView(item: PendingQualificationEstimate): PendingQualificationEstimateView {
    return PendingQualificationEstimateView(
        code = item.code,
        label = item.label,
        quantityText = item.quantity.toString(),
        minimum = "%.2f".format(item.minimum),
        discountText = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingQualificationEstimateViews(items: MutableList<PendingQualificationEstimate>): MutableList<PendingQualificationEstimateView> {
    val views = mutableListOf<PendingQualificationEstimateView>()
    for (item in items) {
        views.add(toPendingQualificationEstimateView(item))
    }
    return views
}

fun sumPendingQualificationEstimateView(view: PendingQualificationEstimateView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    return parsedQuantity.toDouble() + parsedMinimum + parsedDiscount
}
