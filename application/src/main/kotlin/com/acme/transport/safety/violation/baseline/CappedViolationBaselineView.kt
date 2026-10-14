package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped violation baseline. */
data class CappedViolationBaselineView(
    var code: String,
    var nameText: String,
    var title: String,
    var phone: String,
    var countryText: String,
    var trailerId: String,
    var createdAt: String,
    var graceDays: String,
    var routeCode: String,
    var currency: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toCappedViolationBaselineView(item: CappedViolationBaseline): CappedViolationBaselineView {
    return CappedViolationBaselineView(
        code = truncate(item.code, 40),
        nameText = item.name,
        title = item.title,
        phone = item.phone,
        countryText = item.country,
        trailerId = item.trailerId,
        createdAt = item.createdAt.toString(),
        graceDays = item.graceDays.toString(),
        routeCode = truncate(item.routeCode, 80),
        currency = item.currency,
        shiftCode = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedViolationBaselineViews(items: MutableList<CappedViolationBaseline>): MutableList<CappedViolationBaselineView> {
    val views = mutableListOf<CappedViolationBaselineView>()
    for (item in items) {
        views.add(toCappedViolationBaselineView(item))
    }
    return views
}

fun sumCappedViolationBaselineView(view: CappedViolationBaselineView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCreatedAt.toDouble() + parsedGraceDays.toDouble()
}
