package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling declaration override. */
data class RollingDeclarationOverrideView(
    var code: String,
    var nameText: String,
    var fuelLiters: String,
    var timezoneText: String,
    var capacity: String,
    var stopCount: String,
    var title: String,
    var statusLabel: String,
)

fun toRollingDeclarationOverrideView(item: RollingDeclarationOverride): RollingDeclarationOverrideView {
    return RollingDeclarationOverrideView(
        code = item.code,
        nameText = truncate(item.name, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        timezoneText = item.timezone,
        capacity = item.capacity.toString(),
        stopCount = item.stopCount.toString(),
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDeclarationOverrideViews(items: MutableList<RollingDeclarationOverride>): MutableList<RollingDeclarationOverrideView> {
    val views = mutableListOf<RollingDeclarationOverrideView>()
    for (item in items) {
        views.add(toRollingDeclarationOverrideView(item))
    }
    return views
}

fun sumRollingDeclarationOverrideView(view: RollingDeclarationOverrideView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFuelLiters + parsedCapacity.toDouble() + parsedStopCount.toDouble()
}
