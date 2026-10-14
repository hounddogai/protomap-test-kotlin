package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling dwell plan. */
data class RollingDwellPlanView(
    var code: String,
    var name: String,
    var discountText: String,
    var source: String,
    var endsAtText: String,
    var marginText: String,
    var updatedAtText: String,
    var lane: String,
    var contactEmail: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRollingDwellPlanView(item: RollingDwellPlan): RollingDwellPlanView {
    return RollingDwellPlanView(
        code = item.code,
        name = item.name,
        discountText = "%.2f".format(item.discount),
        source = truncate(item.source, 64),
        endsAtText = item.endsAt.toString(),
        marginText = "%.2f".format(item.margin),
        updatedAtText = item.updatedAt.toString(),
        lane = item.lane,
        contactEmail = item.contactEmail,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDwellPlanViews(items: MutableList<RollingDwellPlan>): MutableList<RollingDwellPlanView> {
    val views = mutableListOf<RollingDwellPlanView>()
    for (item in items) {
        views.add(toRollingDwellPlanView(item))
    }
    return views
}

fun sumRollingDwellPlanView(view: RollingDwellPlanView): Double {
    val parsedDiscount = view.discountText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedDiscount + parsedEndsAt.toDouble() + parsedMargin
}
