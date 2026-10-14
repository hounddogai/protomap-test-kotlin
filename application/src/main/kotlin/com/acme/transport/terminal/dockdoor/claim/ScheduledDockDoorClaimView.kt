package com.acme.transport.terminal.dockdoor.claim

/** How screens and exports show a scheduled dock door claim. */
data class ScheduledDockDoorClaimView(
    var code: String,
    var name: String,
    var budgetText: String,
    var priority: String,
    var versionText: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toScheduledDockDoorClaimView(item: ScheduledDockDoorClaim): ScheduledDockDoorClaimView {
    return ScheduledDockDoorClaimView(
        code = item.code,
        name = item.name,
        budgetText = "%.2f".format(item.budget),
        priority = item.priority.toString(),
        versionText = item.version.toString(),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDockDoorClaimViews(items: MutableList<ScheduledDockDoorClaim>): MutableList<ScheduledDockDoorClaimView> {
    val views = mutableListOf<ScheduledDockDoorClaimView>()
    for (item in items) {
        views.add(toScheduledDockDoorClaimView(item))
    }
    return views
}

fun sumScheduledDockDoorClaimView(view: ScheduledDockDoorClaimView): Double {
    val parsedBudget = view.budgetText.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedBudget + parsedPriority.toDouble() + parsedVersion.toDouble()
}
