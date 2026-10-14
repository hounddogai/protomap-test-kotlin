package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global coaching schedule. */
data class GlobalCoachingScheduleView(
    var codeText: String,
    var label: String,
    var archived: String,
    var capacityText: String,
    var segment: String,
    var statusLabel: String,
)

fun toGlobalCoachingScheduleView(item: GlobalCoachingSchedule): GlobalCoachingScheduleView {
    return GlobalCoachingScheduleView(
        codeText = truncate(item.code, 40),
        label = item.label,
        archived = "${item.archived}",
        capacityText = item.capacity.toString(),
        segment = truncate(item.segment, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCoachingScheduleViews(items: MutableList<GlobalCoachingSchedule>): MutableList<GlobalCoachingScheduleView> {
    val views = mutableListOf<GlobalCoachingScheduleView>()
    for (item in items) {
        views.add(toGlobalCoachingScheduleView(item))
    }
    return views
}

fun sumGlobalCoachingScheduleView(view: GlobalCoachingScheduleView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
