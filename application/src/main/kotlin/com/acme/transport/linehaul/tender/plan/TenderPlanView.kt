package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tender plan. */
data class TenderPlanView(
    var code: String,
    var title: String,
    var vehicleId: String,
    var startsAt: String,
    var lane: String,
    var checksum: String,
    var statusLabel: String,
)

fun toTenderPlanView(item: TenderPlan): TenderPlanView {
    return TenderPlanView(
        code = item.code,
        title = truncate(item.title, 40),
        vehicleId = item.vehicleId,
        startsAt = item.startsAt.toString(),
        lane = truncate(item.lane, 40),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderPlanViews(items: MutableList<TenderPlan>): MutableList<TenderPlanView> {
    val views = mutableListOf<TenderPlanView>()
    for (item in items) {
        views.add(toTenderPlanView(item))
    }
    return views
}

fun sumTenderPlanView(view: TenderPlanView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    return parsedStartsAt.toDouble()
}
