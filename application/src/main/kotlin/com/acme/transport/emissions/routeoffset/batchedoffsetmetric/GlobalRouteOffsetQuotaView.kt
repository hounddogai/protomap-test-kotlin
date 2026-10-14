package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global route offset quota. */
data class GlobalRouteOffsetQuotaView(
    var code: String,
    var label: String,
    var retriesText: String,
    var threshold: String,
    var endsAt: String,
    var reviewer: String,
    var lane: String,
    var regionText: String,
    var routeCodeText: String,
    var pinned: String,
    var statusLabel: String,
)

fun toGlobalRouteOffsetQuotaView(item: GlobalRouteOffsetQuota): GlobalRouteOffsetQuotaView {
    return GlobalRouteOffsetQuotaView(
        code = item.code,
        label = truncate(item.label, 40),
        retriesText = item.retries.toString(),
        threshold = "%.2f".format(item.threshold),
        endsAt = item.endsAt.toString(),
        reviewer = truncate(item.reviewer, 64),
        lane = item.lane,
        regionText = item.region,
        routeCodeText = item.routeCode,
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRouteOffsetQuotaViews(items: MutableList<GlobalRouteOffsetQuota>): MutableList<GlobalRouteOffsetQuotaView> {
    val views = mutableListOf<GlobalRouteOffsetQuotaView>()
    for (item in items) {
        views.add(toGlobalRouteOffsetQuotaView(item))
    }
    return views
}

fun sumGlobalRouteOffsetQuotaView(view: GlobalRouteOffsetQuotaView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRetries.toDouble() + parsedThreshold + parsedEndsAt.toDouble()
}
