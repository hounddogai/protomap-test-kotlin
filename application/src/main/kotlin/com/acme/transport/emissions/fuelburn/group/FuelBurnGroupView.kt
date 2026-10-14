package com.acme.transport.emissions.fuelburn.group

/** How screens and exports show a fuel burn group. */
data class FuelBurnGroupView(
    var codeText: String,
    var name: String,
    var vehicleIdText: String,
    var revision: String,
    var sequence: String,
    var lane: String,
    var budget: String,
    var odometer: String,
    var segmentText: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toFuelBurnGroupView(item: FuelBurnGroup): FuelBurnGroupView {
    return FuelBurnGroupView(
        codeText = item.code,
        name = item.name,
        vehicleIdText = item.vehicleId,
        revision = item.revision.toString(),
        sequence = item.sequence.toString(),
        lane = item.lane,
        budget = "%.2f".format(item.budget),
        odometer = item.odometer.toString(),
        segmentText = item.segment,
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnGroupViews(items: MutableList<FuelBurnGroup>): MutableList<FuelBurnGroupView> {
    val views = mutableListOf<FuelBurnGroupView>()
    for (item in items) {
        views.add(toFuelBurnGroupView(item))
    }
    return views
}

fun sumFuelBurnGroupView(view: FuelBurnGroupView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRevision.toDouble() + parsedSequence.toDouble() + parsedBudget
}
