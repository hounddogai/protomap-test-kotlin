package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a violation mapping. */
data class ViolationMappingView(
    var code: String,
    var name: String,
    var durationMinutes: String,
    var loadKgText: String,
    var minimum: String,
    var version: String,
    var summary: String,
    var statusLabel: String,
)

fun toViolationMappingView(item: ViolationMapping): ViolationMappingView {
    return ViolationMappingView(
        code = truncate(item.code, 120),
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        minimum = "%.2f".format(item.minimum),
        version = item.version.toString(),
        summary = truncate(item.summary, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationMappingViews(items: MutableList<ViolationMapping>): MutableList<ViolationMappingView> {
    val views = mutableListOf<ViolationMappingView>()
    for (item in items) {
        views.add(toViolationMappingView(item))
    }
    return views
}

fun sumViolationMappingView(view: ViolationMappingView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedLoadKg + parsedMinimum
}
