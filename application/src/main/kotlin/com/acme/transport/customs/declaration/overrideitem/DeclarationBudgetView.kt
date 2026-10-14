package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration budget. */
data class DeclarationBudgetView(
    var code: String,
    var title: String,
    var dock: String,
    var approvedText: String,
    var carrierText: String,
    var rateText: String,
    var channel: String,
    var category: String,
    var maximumText: String,
    var notes: String,
    var timezone: String,
    var statusLabel: String,
)

fun toDeclarationBudgetView(item: DeclarationBudget): DeclarationBudgetView {
    return DeclarationBudgetView(
        code = truncate(item.code, 120),
        title = item.title,
        dock = item.dock,
        approvedText = "${item.approved}",
        carrierText = item.carrier,
        rateText = "%.2f".format(item.rate),
        channel = truncate(item.channel, 64),
        category = truncate(item.category, 40),
        maximumText = "%.2f".format(item.maximum),
        notes = item.notes,
        timezone = truncate(item.timezone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationBudgetViews(items: MutableList<DeclarationBudget>): MutableList<DeclarationBudgetView> {
    val views = mutableListOf<DeclarationBudgetView>()
    for (item in items) {
        views.add(toDeclarationBudgetView(item))
    }
    return views
}

fun sumDeclarationBudgetView(view: DeclarationBudgetView): Double {
    val parsedRate = view.rateText.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedRate + parsedMaximum
}
