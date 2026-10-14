package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped lane plan threshold. */
data class CappedLanePlanThresholdView(
    var code: String,
    var name: String,
    var shiftCode: String,
    var ratioText: String,
    var vehicleId: String,
    var phone: String,
    var statusLabel: String,
)

fun toCappedLanePlanThresholdView(item: CappedLanePlanThreshold): CappedLanePlanThresholdView {
    return CappedLanePlanThresholdView(
        code = item.code,
        name = item.name,
        shiftCode = truncate(item.shiftCode, 120),
        ratioText = "%.2f".format(item.ratio),
        vehicleId = item.vehicleId,
        phone = truncate(item.phone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLanePlanThresholdViews(items: MutableList<CappedLanePlanThreshold>): MutableList<CappedLanePlanThresholdView> {
    val views = mutableListOf<CappedLanePlanThresholdView>()
    for (item in items) {
        views.add(toCappedLanePlanThresholdView(item))
    }
    return views
}

fun sumCappedLanePlanThresholdView(view: CappedLanePlanThresholdView): Double {
    val parsedRatio = view.ratioText.toDouble()
    return parsedRatio
}
