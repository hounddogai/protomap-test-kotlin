package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled locker group. */
data class ScheduledLockerGroupView(
    var code: String,
    var name: String,
    var driverId: String,
    var loadKgText: String,
    var updatedAt: String,
    var region: String,
    var dockText: String,
    var statusLabel: String,
)

fun toScheduledLockerGroupView(item: ScheduledLockerGroup): ScheduledLockerGroupView {
    return ScheduledLockerGroupView(
        code = truncate(item.code, 120),
        name = item.name,
        driverId = item.driverId,
        loadKgText = "%.2f".format(item.loadKg),
        updatedAt = item.updatedAt.toString(),
        region = truncate(item.region, 64),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLockerGroupViews(items: MutableList<ScheduledLockerGroup>): MutableList<ScheduledLockerGroupView> {
    val views = mutableListOf<ScheduledLockerGroupView>()
    for (item in items) {
        views.add(toScheduledLockerGroupView(item))
    }
    return views
}

fun sumScheduledLockerGroupView(view: ScheduledLockerGroupView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedLoadKg + parsedUpdatedAt.toDouble()
}
