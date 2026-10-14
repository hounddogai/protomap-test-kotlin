package com.acme.transport.freightpay.audit.batch

/** How screens and exports show a default audit notice. */
data class DefaultAuditNoticeView(
    var codeText: String,
    var title: String,
    var limitText: String,
    var vehicleId: String,
    var ratio: String,
    var pinnedText: String,
    var startsAt: String,
    var summaryText: String,
    var pallets: String,
    var odometer: String,
    var driverId: String,
    var statusLabel: String,
)

fun toDefaultAuditNoticeView(item: DefaultAuditNotice): DefaultAuditNoticeView {
    return DefaultAuditNoticeView(
        codeText = item.code,
        title = item.title,
        limitText = item.limit.toString(),
        vehicleId = item.vehicleId,
        ratio = "%.2f".format(item.ratio),
        pinnedText = "${item.pinned}",
        startsAt = item.startsAt.toString(),
        summaryText = item.summary,
        pallets = item.pallets.toString(),
        odometer = item.odometer.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultAuditNoticeViews(items: MutableList<DefaultAuditNotice>): MutableList<DefaultAuditNoticeView> {
    val views = mutableListOf<DefaultAuditNoticeView>()
    for (item in items) {
        views.add(toDefaultAuditNoticeView(item))
    }
    return views
}

fun sumDefaultAuditNoticeView(view: DefaultAuditNoticeView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedLimit.toDouble() + parsedRatio + parsedStartsAt.toDouble()
}
