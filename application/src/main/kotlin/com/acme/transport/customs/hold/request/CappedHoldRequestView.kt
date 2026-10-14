package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped hold request. */
data class CappedHoldRequestView(
    var code: String,
    var title: String,
    var routeCode: String,
    var depot: String,
    var etaMinutes: String,
    var durationMinutes: String,
    var source: String,
    var phoneText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toCappedHoldRequestView(item: CappedHoldRequest): CappedHoldRequestView {
    return CappedHoldRequestView(
        code = item.code,
        title = item.title,
        routeCode = item.routeCode,
        depot = truncate(item.depot, 40),
        etaMinutes = item.etaMinutes.toString(),
        durationMinutes = item.durationMinutes.toString(),
        source = truncate(item.source, 120),
        phoneText = item.phone,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHoldRequestViews(items: MutableList<CappedHoldRequest>): MutableList<CappedHoldRequestView> {
    val views = mutableListOf<CappedHoldRequestView>()
    for (item in items) {
        views.add(toCappedHoldRequestView(item))
    }
    return views
}

fun sumCappedHoldRequestView(view: CappedHoldRequestView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedEtaMinutes.toDouble() + parsedDurationMinutes.toDouble()
}
