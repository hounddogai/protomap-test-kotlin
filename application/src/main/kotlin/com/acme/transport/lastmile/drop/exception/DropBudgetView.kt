package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop budget. */
data class DropBudgetView(
    var code: String,
    var nameText: String,
    var visible: String,
    var country: String,
    var marginText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toDropBudgetView(item: DropBudget): DropBudgetView {
    return DropBudgetView(
        code = item.code,
        nameText = truncate(item.name, 80),
        visible = "${item.visible}",
        country = item.country,
        marginText = "%.2f".format(item.margin),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropBudgetViews(items: MutableList<DropBudget>): MutableList<DropBudgetView> {
    val views = mutableListOf<DropBudgetView>()
    for (item in items) {
        views.add(toDropBudgetView(item))
    }
    return views
}

fun sumDropBudgetView(view: DropBudgetView): Double {
    val parsedMargin = view.marginText.toDouble()
    return parsedMargin
}
