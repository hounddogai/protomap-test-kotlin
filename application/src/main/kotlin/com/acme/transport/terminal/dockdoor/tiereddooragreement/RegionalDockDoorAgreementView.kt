package com.acme.transport.terminal.dockdoor.tiereddooragreement

/** How screens and exports show a regional dock door agreement. */
data class RegionalDockDoorAgreementView(
    var code: String,
    var title: String,
    var priority: String,
    var archived: String,
    var capacity: String,
    var weight: String,
    var lane: String,
    var odometerText: String,
    var budgetText: String,
    var amount: String,
    var statusLabel: String,
)

fun toRegionalDockDoorAgreementView(item: RegionalDockDoorAgreement): RegionalDockDoorAgreementView {
    return RegionalDockDoorAgreementView(
        code = item.code,
        title = item.title,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        capacity = item.capacity.toString(),
        weight = "%.2f".format(item.weight),
        lane = item.lane,
        odometerText = item.odometer.toString(),
        budgetText = "%.2f".format(item.budget),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDockDoorAgreementViews(items: MutableList<RegionalDockDoorAgreement>): MutableList<RegionalDockDoorAgreementView> {
    val views = mutableListOf<RegionalDockDoorAgreementView>()
    for (item in items) {
        views.add(toRegionalDockDoorAgreementView(item))
    }
    return views
}

fun sumRegionalDockDoorAgreementView(view: RegionalDockDoorAgreementView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedCapacity.toDouble() + parsedWeight
}
