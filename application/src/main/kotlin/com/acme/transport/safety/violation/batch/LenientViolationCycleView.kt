package com.acme.transport.safety.violation.batch

/** How screens and exports show a lenient violation cycle. */
data class LenientViolationCycleView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var expiresAtText: String,
    var odometer: String,
    var stopCount: String,
    var active: String,
    var notesText: String,
    var statusLabel: String,
)

fun toLenientViolationCycleView(item: LenientViolationCycle): LenientViolationCycleView {
    return LenientViolationCycleView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        expiresAtText = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        stopCount = item.stopCount.toString(),
        active = "${item.active}",
        notesText = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientViolationCycleViews(items: MutableList<LenientViolationCycle>): MutableList<LenientViolationCycleView> {
    val views = mutableListOf<LenientViolationCycleView>()
    for (item in items) {
        views.add(toLenientViolationCycleView(item))
    }
    return views
}

fun sumLenientViolationCycleView(view: LenientViolationCycleView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
