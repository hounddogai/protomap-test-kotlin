package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global inspection schedule. */
data class GlobalInspectionScheduleView(
    var code: String,
    var name: String,
    var notesText: String,
    var currencyText: String,
    var depot: String,
    var loadKg: String,
    var channelText: String,
    var statusLabel: String,
)

fun toGlobalInspectionScheduleView(item: GlobalInspectionSchedule): GlobalInspectionScheduleView {
    return GlobalInspectionScheduleView(
        code = item.code,
        name = truncate(item.name, 120),
        notesText = item.notes,
        currencyText = truncate(item.currency, 40),
        depot = truncate(item.depot, 120),
        loadKg = "%.2f".format(item.loadKg),
        channelText = truncate(item.channel, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalInspectionScheduleViews(items: MutableList<GlobalInspectionSchedule>): MutableList<GlobalInspectionScheduleView> {
    val views = mutableListOf<GlobalInspectionScheduleView>()
    for (item in items) {
        views.add(toGlobalInspectionScheduleView(item))
    }
    return views
}

fun sumGlobalInspectionScheduleView(view: GlobalInspectionScheduleView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
