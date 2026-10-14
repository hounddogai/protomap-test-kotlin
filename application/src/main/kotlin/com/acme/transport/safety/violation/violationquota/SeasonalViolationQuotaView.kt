package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal violation quota. */
data class SeasonalViolationQuotaView(
    var code: String,
    var labelText: String,
    var endsAt: String,
    var floorValue: String,
    var description: String,
    var timezone: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toSeasonalViolationQuotaView(item: SeasonalViolationQuota): SeasonalViolationQuotaView {
    return SeasonalViolationQuotaView(
        code = item.code,
        labelText = item.label,
        endsAt = item.endsAt.toString(),
        floorValue = "%.2f".format(item.floorValue),
        description = item.description,
        timezone = truncate(item.timezone, 120),
        shiftCode = truncate(item.shiftCode, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalViolationQuotaViews(items: MutableList<SeasonalViolationQuota>): MutableList<SeasonalViolationQuotaView> {
    val views = mutableListOf<SeasonalViolationQuotaView>()
    for (item in items) {
        views.add(toSeasonalViolationQuotaView(item))
    }
    return views
}

fun sumSeasonalViolationQuotaView(view: SeasonalViolationQuotaView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedEndsAt.toDouble() + parsedFloorValue
}
