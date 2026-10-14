package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback lane plan notice. */
data class FallbackLanePlanNoticeView(
    var code: String,
    var title: String,
    var timezoneText: String,
    var ratio: String,
    var floorValue: String,
    var maximum: String,
    var trailerId: String,
    var depot: String,
    var name: String,
    var statusLabel: String,
)

fun toFallbackLanePlanNoticeView(item: FallbackLanePlanNotice): FallbackLanePlanNoticeView {
    return FallbackLanePlanNoticeView(
        code = truncate(item.code, 64),
        title = item.title,
        timezoneText = item.timezone,
        ratio = "%.2f".format(item.ratio),
        floorValue = "%.2f".format(item.floorValue),
        maximum = "%.2f".format(item.maximum),
        trailerId = item.trailerId,
        depot = truncate(item.depot, 80),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLanePlanNoticeViews(items: MutableList<FallbackLanePlanNotice>): MutableList<FallbackLanePlanNoticeView> {
    val views = mutableListOf<FallbackLanePlanNoticeView>()
    for (item in items) {
        views.add(toFallbackLanePlanNoticeView(item))
    }
    return views
}

fun sumFallbackLanePlanNoticeView(view: FallbackLanePlanNoticeView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedMaximum = view.maximum.toDouble()
    return parsedRatio + parsedFloorValue + parsedMaximum
}
