package com.acme.transport.emissions.fuelburn.plan

/** How screens and exports show a tiered fuel burn plan. */
data class TieredFuelBurnPlanView(
    var code: String,
    var title: String,
    var externalRef: String,
    var endsAt: String,
    var distanceKm: String,
    var label: String,
    var active: String,
    var statusLabel: String,
)

fun toTieredFuelBurnPlanView(item: TieredFuelBurnPlan): TieredFuelBurnPlanView {
    return TieredFuelBurnPlanView(
        code = item.code,
        title = item.title,
        externalRef = item.externalRef,
        endsAt = item.endsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        label = item.label,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelBurnPlanViews(items: MutableList<TieredFuelBurnPlan>): MutableList<TieredFuelBurnPlanView> {
    val views = mutableListOf<TieredFuelBurnPlanView>()
    for (item in items) {
        views.add(toTieredFuelBurnPlanView(item))
    }
    return views
}

fun sumTieredFuelBurnPlanView(view: TieredFuelBurnPlanView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedEndsAt.toDouble() + parsedDistanceKm
}
