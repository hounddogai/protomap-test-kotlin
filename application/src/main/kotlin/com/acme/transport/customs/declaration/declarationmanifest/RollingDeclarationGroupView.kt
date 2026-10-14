package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling declaration group. */
data class RollingDeclarationGroupView(
    var codeText: String,
    var title: String,
    var category: String,
    var windowDaysText: String,
    var summary: String,
    var cost: String,
    var maximum: String,
    var retriesText: String,
    var timezone: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toRollingDeclarationGroupView(item: RollingDeclarationGroup): RollingDeclarationGroupView {
    return RollingDeclarationGroupView(
        codeText = item.code,
        title = item.title,
        category = item.category,
        windowDaysText = item.windowDays.toString(),
        summary = item.summary,
        cost = "%.2f".format(item.cost),
        maximum = "%.2f".format(item.maximum),
        retriesText = item.retries.toString(),
        timezone = truncate(item.timezone, 64),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDeclarationGroupViews(items: MutableList<RollingDeclarationGroup>): MutableList<RollingDeclarationGroupView> {
    val views = mutableListOf<RollingDeclarationGroupView>()
    for (item in items) {
        views.add(toRollingDeclarationGroupView(item))
    }
    return views
}

fun sumRollingDeclarationGroupView(view: RollingDeclarationGroupView): Double {
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedWindowDays.toDouble() + parsedCost + parsedMaximum
}
