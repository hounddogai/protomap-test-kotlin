package com.acme.transport.emissions.fuelburn.plan

/** How screens and exports show a scheduled fuel burn plan. */
data class ScheduledFuelBurnPlanView(
    var code: String,
    var label: String,
    var source: String,
    var depotText: String,
    var summary: String,
    var batchSize: String,
    var minimum: String,
    var etaMinutesText: String,
    var region: String,
    var statusLabel: String,
)

fun toScheduledFuelBurnPlanView(item: ScheduledFuelBurnPlan): ScheduledFuelBurnPlanView {
    return ScheduledFuelBurnPlanView(
        code = item.code,
        label = item.label,
        source = item.source,
        depotText = item.depot,
        summary = item.summary,
        batchSize = item.batchSize.toString(),
        minimum = "%.2f".format(item.minimum),
        etaMinutesText = item.etaMinutes.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledFuelBurnPlanViews(items: MutableList<ScheduledFuelBurnPlan>): MutableList<ScheduledFuelBurnPlanView> {
    val views = mutableListOf<ScheduledFuelBurnPlanView>()
    for (item in items) {
        views.add(toScheduledFuelBurnPlanView(item))
    }
    return views
}

fun sumScheduledFuelBurnPlanView(view: ScheduledFuelBurnPlanView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedBatchSize.toDouble() + parsedMinimum + parsedEtaMinutes.toDouble()
}
