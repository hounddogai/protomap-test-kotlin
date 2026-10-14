package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive duty period segment. */
data class AdaptiveDutyPeriodSegmentView(
    var code: String,
    var name: String,
    var title: String,
    var region: String,
    var ownerId: String,
    var segment: String,
    var marginText: String,
    var discountText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toAdaptiveDutyPeriodSegmentView(item: AdaptiveDutyPeriodSegment): AdaptiveDutyPeriodSegmentView {
    return AdaptiveDutyPeriodSegmentView(
        code = item.code,
        name = truncate(item.name, 64),
        title = item.title,
        region = item.region,
        ownerId = truncate(item.ownerId, 80),
        segment = truncate(item.segment, 120),
        marginText = "%.2f".format(item.margin),
        discountText = "%.2f".format(item.discount),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveDutyPeriodSegmentViews(items: MutableList<AdaptiveDutyPeriodSegment>): MutableList<AdaptiveDutyPeriodSegmentView> {
    val views = mutableListOf<AdaptiveDutyPeriodSegmentView>()
    for (item in items) {
        views.add(toAdaptiveDutyPeriodSegmentView(item))
    }
    return views
}

fun sumAdaptiveDutyPeriodSegmentView(view: AdaptiveDutyPeriodSegmentView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedDiscount = view.discountText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedMargin + parsedDiscount + parsedDurationMinutes.toDouble()
}
