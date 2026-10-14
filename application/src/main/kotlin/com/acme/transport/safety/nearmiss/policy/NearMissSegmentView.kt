package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss segment. */
data class NearMissSegmentView(
    var code: String,
    var labelText: String,
    var endsAt: String,
    var driverId: String,
    var routeCodeText: String,
    var regionText: String,
    var statusLabel: String,
)

fun toNearMissSegmentView(item: NearMissSegment): NearMissSegmentView {
    return NearMissSegmentView(
        code = item.code,
        labelText = item.label,
        endsAt = item.endsAt.toString(),
        driverId = truncate(item.driverId, 80),
        routeCodeText = item.routeCode,
        regionText = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissSegmentViews(items: MutableList<NearMissSegment>): MutableList<NearMissSegmentView> {
    val views = mutableListOf<NearMissSegmentView>()
    for (item in items) {
        views.add(toNearMissSegmentView(item))
    }
    return views
}

fun sumNearMissSegmentView(view: NearMissSegmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    return parsedEndsAt.toDouble()
}
