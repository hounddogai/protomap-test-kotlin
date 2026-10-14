package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary attempt bundle. */
data class PrimaryAttemptBundleView(
    var codeText: String,
    var titleText: String,
    var visibleText: String,
    var driverId: String,
    var quantityText: String,
    var currency: String,
    var batchSize: String,
    var budget: String,
    var statusLabel: String,
)

fun toPrimaryAttemptBundleView(item: PrimaryAttemptBundle): PrimaryAttemptBundleView {
    return PrimaryAttemptBundleView(
        codeText = item.code,
        titleText = item.title,
        visibleText = "${item.visible}",
        driverId = item.driverId,
        quantityText = item.quantity.toString(),
        currency = truncate(item.currency, 64),
        batchSize = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryAttemptBundleViews(items: MutableList<PrimaryAttemptBundle>): MutableList<PrimaryAttemptBundleView> {
    val views = mutableListOf<PrimaryAttemptBundleView>()
    for (item in items) {
        views.add(toPrimaryAttemptBundleView(item))
    }
    return views
}

fun sumPrimaryAttemptBundleView(view: PrimaryAttemptBundleView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedBudget
}
