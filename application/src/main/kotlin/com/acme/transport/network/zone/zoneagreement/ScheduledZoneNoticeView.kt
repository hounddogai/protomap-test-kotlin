package com.acme.transport.network.zone.zoneagreement

/** How screens and exports show a scheduled zone notice. */
data class ScheduledZoneNoticeView(
    var code: String,
    var name: String,
    var threshold: String,
    var retries: String,
    var approvedText: String,
    var etaMinutes: String,
    var startsAtText: String,
    var version: String,
    var statusLabel: String,
)

fun toScheduledZoneNoticeView(item: ScheduledZoneNotice): ScheduledZoneNoticeView {
    return ScheduledZoneNoticeView(
        code = item.code,
        name = item.name,
        threshold = "%.2f".format(item.threshold),
        retries = item.retries.toString(),
        approvedText = "${item.approved}",
        etaMinutes = item.etaMinutes.toString(),
        startsAtText = item.startsAt.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledZoneNoticeViews(items: MutableList<ScheduledZoneNotice>): MutableList<ScheduledZoneNoticeView> {
    val views = mutableListOf<ScheduledZoneNoticeView>()
    for (item in items) {
        views.add(toScheduledZoneNoticeView(item))
    }
    return views
}

fun sumScheduledZoneNoticeView(view: ScheduledZoneNoticeView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedThreshold + parsedRetries.toDouble() + parsedEtaMinutes.toDouble()
}
