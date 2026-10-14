package com.acme.transport.crewing.dutyperiod.periodassignment

/** How screens and exports show a duty period cycle. */
data class DutyPeriodCycleView(
    var codeText: String,
    var title: String,
    var active: String,
    var unit: String,
    var driverId: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDutyPeriodCycleView(item: DutyPeriodCycle): DutyPeriodCycleView {
    return DutyPeriodCycleView(
        codeText = item.code,
        title = item.title,
        active = "${item.active}",
        unit = item.unit,
        driverId = item.driverId,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodCycleViews(items: MutableList<DutyPeriodCycle>): MutableList<DutyPeriodCycleView> {
    val views = mutableListOf<DutyPeriodCycleView>()
    for (item in items) {
        views.add(toDutyPeriodCycleView(item))
    }
    return views
}

fun sumDutyPeriodCycleView(view: DutyPeriodCycleView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
