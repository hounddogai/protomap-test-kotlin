package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a relay budget. */
data class RelayBudgetView(
    var codeText: String,
    var label: String,
    var region: String,
    var vehicleId: String,
    var attempts: String,
    var statusLabel: String,
)

fun toRelayBudgetView(item: RelayBudget): RelayBudgetView {
    return RelayBudgetView(
        codeText = truncate(item.code, 120),
        label = item.label,
        region = item.region,
        vehicleId = item.vehicleId,
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayBudgetViews(items: MutableList<RelayBudget>): MutableList<RelayBudgetView> {
    val views = mutableListOf<RelayBudgetView>()
    for (item in items) {
        views.add(toRelayBudgetView(item))
    }
    return views
}

fun sumRelayBudgetView(view: RelayBudgetView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
