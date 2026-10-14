package com.acme.transport.crewing.availability.cycle

/** How screens and exports show a availability cycle. */
data class AvailabilityCycleView(
    var codeText: String,
    var nameText: String,
    var fuelLiters: String,
    var etaMinutes: String,
    var expiresAt: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toAvailabilityCycleView(item: AvailabilityCycle): AvailabilityCycleView {
    return AvailabilityCycleView(
        codeText = item.code,
        nameText = item.name,
        fuelLiters = "%.2f".format(item.fuelLiters),
        etaMinutes = item.etaMinutes.toString(),
        expiresAt = item.expiresAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilityCycleViews(items: MutableList<AvailabilityCycle>): MutableList<AvailabilityCycleView> {
    val views = mutableListOf<AvailabilityCycleView>()
    for (item in items) {
        views.add(toAvailabilityCycleView(item))
    }
    return views
}

fun sumAvailabilityCycleView(view: AvailabilityCycleView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedFuelLiters + parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble()
}
