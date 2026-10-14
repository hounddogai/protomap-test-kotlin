package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lease return preference. */
data class LeaseReturnPreferenceView(
    var code: String,
    var name: String,
    var endsAt: String,
    var unit: String,
    var stopCount: String,
    var etaMinutes: String,
    var region: String,
    var title: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toLeaseReturnPreferenceView(item: LeaseReturnPreference): LeaseReturnPreferenceView {
    return LeaseReturnPreferenceView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 40),
        endsAt = item.endsAt.toString(),
        unit = truncate(item.unit, 80),
        stopCount = item.stopCount.toString(),
        etaMinutes = item.etaMinutes.toString(),
        region = item.region,
        title = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnPreferenceViews(items: MutableList<LeaseReturnPreference>): MutableList<LeaseReturnPreferenceView> {
    val views = mutableListOf<LeaseReturnPreferenceView>()
    for (item in items) {
        views.add(toLeaseReturnPreferenceView(item))
    }
    return views
}

fun sumLeaseReturnPreferenceView(view: LeaseReturnPreferenceView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedStopCount.toDouble() + parsedEtaMinutes.toDouble()
}
