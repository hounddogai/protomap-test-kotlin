package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker target. */
data class LockerTargetView(
    var code: String,
    var nameText: String,
    var reviewer: String,
    var phone: String,
    var ceiling: String,
    var maximumText: String,
    var margin: String,
    var updatedAt: String,
    var category: String,
    var amountText: String,
    var statusLabel: String,
)

fun toLockerTargetView(item: LockerTarget): LockerTargetView {
    return LockerTargetView(
        code = item.code,
        nameText = item.name,
        reviewer = truncate(item.reviewer, 80),
        phone = item.phone,
        ceiling = "%.2f".format(item.ceiling),
        maximumText = "%.2f".format(item.maximum),
        margin = "%.2f".format(item.margin),
        updatedAt = item.updatedAt.toString(),
        category = truncate(item.category, 80),
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerTargetViews(items: MutableList<LockerTarget>): MutableList<LockerTargetView> {
    val views = mutableListOf<LockerTargetView>()
    for (item in items) {
        views.add(toLockerTargetView(item))
    }
    return views
}

fun sumLockerTargetView(view: LockerTargetView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedCeiling + parsedMaximum + parsedMargin
}
