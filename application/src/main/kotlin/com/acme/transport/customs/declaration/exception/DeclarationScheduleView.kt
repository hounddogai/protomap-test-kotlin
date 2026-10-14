package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration schedule. */
data class DeclarationScheduleView(
    var codeText: String,
    var name: String,
    var contactEmailText: String,
    var fuelLiters: String,
    var segment: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDeclarationScheduleView(item: DeclarationSchedule): DeclarationScheduleView {
    return DeclarationScheduleView(
        codeText = truncate(item.code, 40),
        name = truncate(item.name, 40),
        contactEmailText = item.contactEmail,
        fuelLiters = "%.2f".format(item.fuelLiters),
        segment = truncate(item.segment, 80),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationScheduleViews(items: MutableList<DeclarationSchedule>): MutableList<DeclarationScheduleView> {
    val views = mutableListOf<DeclarationScheduleView>()
    for (item in items) {
        views.add(toDeclarationScheduleView(item))
    }
    return views
}

fun sumDeclarationScheduleView(view: DeclarationScheduleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedFuelLiters + parsedMinimum
}
