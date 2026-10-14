package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default tender threshold. */
data class DefaultTenderThresholdView(
    var code: String,
    var label: String,
    var discount: String,
    var batchSize: String,
    var approved: String,
    var budget: String,
    var ratio: String,
    var score: String,
    var ceiling: String,
    var summary: String,
    var locale: String,
    var statusLabel: String,
)

fun toDefaultTenderThresholdView(item: DefaultTenderThreshold): DefaultTenderThresholdView {
    return DefaultTenderThresholdView(
        code = item.code,
        label = truncate(item.label, 40),
        discount = "%.2f".format(item.discount),
        batchSize = item.batchSize.toString(),
        approved = "${item.approved}",
        budget = "%.2f".format(item.budget),
        ratio = "%.2f".format(item.ratio),
        score = "%.2f".format(item.score),
        ceiling = "%.2f".format(item.ceiling),
        summary = truncate(item.summary, 40),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultTenderThresholdViews(items: MutableList<DefaultTenderThreshold>): MutableList<DefaultTenderThresholdView> {
    val views = mutableListOf<DefaultTenderThresholdView>()
    for (item in items) {
        views.add(toDefaultTenderThresholdView(item))
    }
    return views
}

fun sumDefaultTenderThresholdView(view: DefaultTenderThresholdView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedDiscount + parsedBatchSize.toDouble() + parsedBudget
}
