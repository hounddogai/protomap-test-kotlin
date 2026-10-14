package com.acme.transport.lastmile.redelivery.forecast

/** How screens and exports show a scheduled redelivery notice. */
data class ScheduledRedeliveryNoticeView(
    var code: String,
    var title: String,
    var expiresAt: String,
    var etaMinutesText: String,
    var driverId: String,
    var country: String,
    var statusLabel: String,
)

fun toScheduledRedeliveryNoticeView(item: ScheduledRedeliveryNotice): ScheduledRedeliveryNoticeView {
    return ScheduledRedeliveryNoticeView(
        code = item.code,
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        driverId = item.driverId,
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRedeliveryNoticeViews(items: MutableList<ScheduledRedeliveryNotice>): MutableList<ScheduledRedeliveryNoticeView> {
    val views = mutableListOf<ScheduledRedeliveryNoticeView>()
    for (item in items) {
        views.add(toScheduledRedeliveryNoticeView(item))
    }
    return views
}

fun sumScheduledRedeliveryNoticeView(view: ScheduledRedeliveryNoticeView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
