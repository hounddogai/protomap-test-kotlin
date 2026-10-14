package com.acme.transport.emissions.evcharge.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient ev charge milestone. */
data class LenientEvChargeMilestoneView(
    var codeText: String,
    var name: String,
    var odometerText: String,
    var active: String,
    var ownerId: String,
    var description: String,
    var shiftCode: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLenientEvChargeMilestoneView(item: LenientEvChargeMilestone): LenientEvChargeMilestoneView {
    return LenientEvChargeMilestoneView(
        codeText = item.code,
        name = item.name,
        odometerText = item.odometer.toString(),
        active = "${item.active}",
        ownerId = truncate(item.ownerId, 80),
        description = item.description,
        shiftCode = item.shiftCode,
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientEvChargeMilestoneViews(items: MutableList<LenientEvChargeMilestone>): MutableList<LenientEvChargeMilestoneView> {
    val views = mutableListOf<LenientEvChargeMilestoneView>()
    for (item in items) {
        views.add(toLenientEvChargeMilestoneView(item))
    }
    return views
}

fun sumLenientEvChargeMilestoneView(view: LenientEvChargeMilestoneView): Double {
    val parsedOdometer = view.odometerText.toInt()
    return parsedOdometer.toDouble()
}
