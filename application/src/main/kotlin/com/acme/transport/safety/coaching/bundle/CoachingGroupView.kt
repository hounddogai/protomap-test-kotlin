package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a coaching group. */
data class CoachingGroupView(
    var code: String,
    var name: String,
    var discountText: String,
    var vehicleId: String,
    var checksum: String,
    var driverId: String,
    var statusLabel: String,
)

fun toCoachingGroupView(item: CoachingGroup): CoachingGroupView {
    return CoachingGroupView(
        code = truncate(item.code, 120),
        name = item.name,
        discountText = "%.2f".format(item.discount),
        vehicleId = truncate(item.vehicleId, 120),
        checksum = item.checksum,
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCoachingGroupViews(items: MutableList<CoachingGroup>): MutableList<CoachingGroupView> {
    val views = mutableListOf<CoachingGroupView>()
    for (item in items) {
        views.add(toCoachingGroupView(item))
    }
    return views
}

fun sumCoachingGroupView(view: CoachingGroupView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
