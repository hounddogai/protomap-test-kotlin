package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area notice. */
data class ServiceAreaNoticeView(
    var code: String,
    var nameText: String,
    var minimum: String,
    var stopCount: String,
    var routeCode: String,
    var notes: String,
    var etaMinutes: String,
    var visible: String,
    var statusLabel: String,
)

fun toServiceAreaNoticeView(item: ServiceAreaNotice): ServiceAreaNoticeView {
    return ServiceAreaNoticeView(
        code = truncate(item.code, 40),
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        routeCode = truncate(item.routeCode, 80),
        notes = item.notes,
        etaMinutes = item.etaMinutes.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaNoticeViews(items: MutableList<ServiceAreaNotice>): MutableList<ServiceAreaNoticeView> {
    val views = mutableListOf<ServiceAreaNoticeView>()
    for (item in items) {
        views.add(toServiceAreaNoticeView(item))
    }
    return views
}

fun sumServiceAreaNoticeView(view: ServiceAreaNoticeView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedMinimum + parsedStopCount.toDouble() + parsedEtaMinutes.toDouble()
}
