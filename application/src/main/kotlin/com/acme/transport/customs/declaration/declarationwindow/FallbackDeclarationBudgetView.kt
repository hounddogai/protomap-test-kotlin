package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback declaration budget. */
data class FallbackDeclarationBudgetView(
    var code: String,
    var name: String,
    var channelText: String,
    var dock: String,
    var version: String,
    var cost: String,
    var updatedAt: String,
    var floorValue: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toFallbackDeclarationBudgetView(item: FallbackDeclarationBudget): FallbackDeclarationBudgetView {
    return FallbackDeclarationBudgetView(
        code = truncate(item.code, 64),
        name = truncate(item.name, 80),
        channelText = truncate(item.channel, 80),
        dock = truncate(item.dock, 40),
        version = item.version.toString(),
        cost = "%.2f".format(item.cost),
        updatedAt = item.updatedAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDeclarationBudgetViews(items: MutableList<FallbackDeclarationBudget>): MutableList<FallbackDeclarationBudgetView> {
    val views = mutableListOf<FallbackDeclarationBudgetView>()
    for (item in items) {
        views.add(toFallbackDeclarationBudgetView(item))
    }
    return views
}

fun sumFallbackDeclarationBudgetView(view: FallbackDeclarationBudgetView): Double {
    val parsedVersion = view.version.toInt()
    val parsedCost = view.cost.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedVersion.toDouble() + parsedCost + parsedUpdatedAt.toDouble()
}
