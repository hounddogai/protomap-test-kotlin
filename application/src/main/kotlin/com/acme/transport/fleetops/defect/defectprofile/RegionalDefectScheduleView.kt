package com.acme.transport.fleetops.defect.defectprofile

/** How screens and exports show a regional defect schedule. */
data class RegionalDefectScheduleView(
    var codeText: String,
    var label: String,
    var contactEmailText: String,
    var driverIdText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toRegionalDefectScheduleView(item: RegionalDefectSchedule): RegionalDefectScheduleView {
    return RegionalDefectScheduleView(
        codeText = item.code,
        label = item.label,
        contactEmailText = item.contactEmail,
        driverIdText = item.driverId,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDefectScheduleViews(items: MutableList<RegionalDefectSchedule>): MutableList<RegionalDefectScheduleView> {
    val views = mutableListOf<RegionalDefectScheduleView>()
    for (item in items) {
        views.add(toRegionalDefectScheduleView(item))
    }
    return views
}

fun sumRegionalDefectScheduleView(view: RegionalDefectScheduleView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedDurationMinutes.toDouble()
}
