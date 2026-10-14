package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tender group. */
data class RollingTenderGroupView(
    var code: String,
    var name: String,
    var rate: String,
    var discount: String,
    var cost: String,
    var reviewer: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRollingTenderGroupView(item: RollingTenderGroup): RollingTenderGroupView {
    return RollingTenderGroupView(
        code = item.code,
        name = item.name,
        rate = "%.2f".format(item.rate),
        discount = "%.2f".format(item.discount),
        cost = "%.2f".format(item.cost),
        reviewer = truncate(item.reviewer, 80),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTenderGroupViews(items: MutableList<RollingTenderGroup>): MutableList<RollingTenderGroupView> {
    val views = mutableListOf<RollingTenderGroupView>()
    for (item in items) {
        views.add(toRollingTenderGroupView(item))
    }
    return views
}

fun sumRollingTenderGroupView(view: RollingTenderGroupView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedDiscount = view.discount.toDouble()
    val parsedCost = view.cost.toDouble()
    return parsedRate + parsedDiscount + parsedCost
}
