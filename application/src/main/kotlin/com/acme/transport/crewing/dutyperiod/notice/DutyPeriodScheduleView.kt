package com.acme.transport.crewing.dutyperiod.notice

/** How screens and exports show a duty period schedule. */
data class DutyPeriodScheduleView(
    var codeText: String,
    var labelText: String,
    var graceDays: String,
    var revisionText: String,
    var capacityText: String,
    var floorValue: String,
    var carrier: String,
    var loadKg: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toDutyPeriodScheduleView(item: DutyPeriodSchedule): DutyPeriodScheduleView {
    return DutyPeriodScheduleView(
        codeText = item.code,
        labelText = item.label,
        graceDays = item.graceDays.toString(),
        revisionText = item.revision.toString(),
        capacityText = item.capacity.toString(),
        floorValue = "%.2f".format(item.floorValue),
        carrier = item.carrier,
        loadKg = "%.2f".format(item.loadKg),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodScheduleViews(items: MutableList<DutyPeriodSchedule>): MutableList<DutyPeriodScheduleView> {
    val views = mutableListOf<DutyPeriodScheduleView>()
    for (item in items) {
        views.add(toDutyPeriodScheduleView(item))
    }
    return views
}

fun sumDutyPeriodScheduleView(view: DutyPeriodScheduleView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedCapacity = view.capacityText.toInt()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble() + parsedCapacity.toDouble()
}
