package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled rest break digest. */
data class ScheduledRestBreakDigestView(
    var code: String,
    var title: String,
    var dock: String,
    var carrierText: String,
    var currency: String,
    var durationMinutesText: String,
    var driverId: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toScheduledRestBreakDigestView(item: ScheduledRestBreakDigest): ScheduledRestBreakDigestView {
    return ScheduledRestBreakDigestView(
        code = item.code,
        title = item.title,
        dock = item.dock,
        carrierText = truncate(item.carrier, 64),
        currency = item.currency,
        durationMinutesText = item.durationMinutes.toString(),
        driverId = item.driverId,
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRestBreakDigestViews(items: MutableList<ScheduledRestBreakDigest>): MutableList<ScheduledRestBreakDigestView> {
    val views = mutableListOf<ScheduledRestBreakDigestView>()
    for (item in items) {
        views.add(toScheduledRestBreakDigestView(item))
    }
    return views
}

fun sumScheduledRestBreakDigestView(view: ScheduledRestBreakDigestView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
