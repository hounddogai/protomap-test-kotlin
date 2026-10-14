package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a route offset notice. */
data class RouteOffsetNoticeView(
    var code: String,
    var label: String,
    var attempts: String,
    var discountText: String,
    var rate: String,
    var title: String,
    var statusLabel: String,
)

fun toRouteOffsetNoticeView(item: RouteOffsetNotice): RouteOffsetNoticeView {
    return RouteOffsetNoticeView(
        code = truncate(item.code, 120),
        label = item.label,
        attempts = item.attempts.toString(),
        discountText = "%.2f".format(item.discount),
        rate = "%.2f".format(item.rate),
        title = truncate(item.title, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRouteOffsetNoticeViews(items: MutableList<RouteOffsetNotice>): MutableList<RouteOffsetNoticeView> {
    val views = mutableListOf<RouteOffsetNoticeView>()
    for (item in items) {
        views.add(toRouteOffsetNoticeView(item))
    }
    return views
}

fun sumRouteOffsetNoticeView(view: RouteOffsetNoticeView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedRate = view.rate.toDouble()
    return parsedAttempts.toDouble() + parsedDiscount + parsedRate
}
