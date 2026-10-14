package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan profile. */
data class FallbackServicePlanProfileView(
    var code: String,
    var label: String,
    var carrier: String,
    var stopCountText: String,
    var revision: String,
    var statusLabel: String,
)

fun toFallbackServicePlanProfileView(item: FallbackServicePlanProfile): FallbackServicePlanProfileView {
    return FallbackServicePlanProfileView(
        code = item.code,
        label = truncate(item.label, 40),
        carrier = item.carrier,
        stopCountText = item.stopCount.toString(),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanProfileViews(items: MutableList<FallbackServicePlanProfile>): MutableList<FallbackServicePlanProfileView> {
    val views = mutableListOf<FallbackServicePlanProfileView>()
    for (item in items) {
        views.add(toFallbackServicePlanProfileView(item))
    }
    return views
}

fun sumFallbackServicePlanProfileView(view: FallbackServicePlanProfileView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedStopCount.toDouble() + parsedRevision.toDouble()
}
