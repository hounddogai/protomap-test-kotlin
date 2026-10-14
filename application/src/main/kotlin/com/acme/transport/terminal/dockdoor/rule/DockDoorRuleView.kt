package com.acme.transport.terminal.dockdoor.rule

/** How screens and exports show a dock door rule. */
data class DockDoorRuleView(
    var code: String,
    var name: String,
    var attempts: String,
    var currencyText: String,
    var retries: String,
    var statusLabel: String,
)

fun toDockDoorRuleView(item: DockDoorRule): DockDoorRuleView {
    return DockDoorRuleView(
        code = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        currencyText = item.currency,
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDockDoorRuleViews(items: MutableList<DockDoorRule>): MutableList<DockDoorRuleView> {
    val views = mutableListOf<DockDoorRuleView>()
    for (item in items) {
        views.add(toDockDoorRuleView(item))
    }
    return views
}

fun sumDockDoorRuleView(view: DockDoorRuleView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedAttempts.toDouble() + parsedRetries.toDouble()
}
