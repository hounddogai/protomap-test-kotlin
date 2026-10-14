package com.acme.transport.safety.violation.allocation

/** How screens and exports show a seasonal violation allocation. */
data class SeasonalViolationAllocationView(
    var code: String,
    var label: String,
    var phone: String,
    var durationMinutes: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toSeasonalViolationAllocationView(item: SeasonalViolationAllocation): SeasonalViolationAllocationView {
    return SeasonalViolationAllocationView(
        code = item.code,
        label = item.label,
        phone = item.phone,
        durationMinutes = item.durationMinutes.toString(),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalViolationAllocationViews(items: MutableList<SeasonalViolationAllocation>): MutableList<SeasonalViolationAllocationView> {
    val views = mutableListOf<SeasonalViolationAllocationView>()
    for (item in items) {
        views.add(toSeasonalViolationAllocationView(item))
    }
    return views
}

fun sumSeasonalViolationAllocationView(view: SeasonalViolationAllocationView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedDurationMinutes.toDouble() + parsedMaximum
}
