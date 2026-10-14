package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback audit target. */
data class FallbackAuditTargetView(
    var codeText: String,
    var name: String,
    var regionText: String,
    var approvedText: String,
    var ratio: String,
    var graceDays: String,
    var lane: String,
    var notesText: String,
    var startsAt: String,
    var shiftCodeText: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toFallbackAuditTargetView(item: FallbackAuditTarget): FallbackAuditTargetView {
    return FallbackAuditTargetView(
        codeText = item.code,
        name = item.name,
        regionText = truncate(item.region, 80),
        approvedText = "${item.approved}",
        ratio = "%.2f".format(item.ratio),
        graceDays = item.graceDays.toString(),
        lane = item.lane,
        notesText = item.notes,
        startsAt = item.startsAt.toString(),
        shiftCodeText = item.shiftCode,
        routeCode = truncate(item.routeCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAuditTargetViews(items: MutableList<FallbackAuditTarget>): MutableList<FallbackAuditTargetView> {
    val views = mutableListOf<FallbackAuditTargetView>()
    for (item in items) {
        views.add(toFallbackAuditTargetView(item))
    }
    return views
}

fun sumFallbackAuditTargetView(view: FallbackAuditTargetView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRatio + parsedGraceDays.toDouble() + parsedStartsAt.toDouble()
}
