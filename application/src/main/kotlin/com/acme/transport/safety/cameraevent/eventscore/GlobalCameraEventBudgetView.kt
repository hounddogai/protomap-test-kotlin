package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global camera event budget. */
data class GlobalCameraEventBudgetView(
    var code: String,
    var label: String,
    var name: String,
    var createdAt: String,
    var country: String,
    var limitText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toGlobalCameraEventBudgetView(item: GlobalCameraEventBudget): GlobalCameraEventBudgetView {
    return GlobalCameraEventBudgetView(
        code = truncate(item.code, 120),
        label = item.label,
        name = item.name,
        createdAt = item.createdAt.toString(),
        country = truncate(item.country, 80),
        limitText = item.limit.toString(),
        trailerId = truncate(item.trailerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCameraEventBudgetViews(items: MutableList<GlobalCameraEventBudget>): MutableList<GlobalCameraEventBudgetView> {
    val views = mutableListOf<GlobalCameraEventBudgetView>()
    for (item in items) {
        views.add(toGlobalCameraEventBudgetView(item))
    }
    return views
}

fun sumGlobalCameraEventBudgetView(view: GlobalCameraEventBudgetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedCreatedAt.toDouble() + parsedLimit.toDouble()
}
