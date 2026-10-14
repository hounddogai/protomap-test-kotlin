package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary transit time notice. */
data class PrimaryTransitTimeNoticeView(
    var code: String,
    var label: String,
    var summary: String,
    var attemptsText: String,
    var checksumText: String,
    var createdAt: String,
    var category: String,
    var updatedAtText: String,
    var region: String,
    var driverId: String,
    var budget: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeNoticeView(item: PrimaryTransitTimeNotice): PrimaryTransitTimeNoticeView {
    return PrimaryTransitTimeNoticeView(
        code = truncate(item.code, 40),
        label = item.label,
        summary = item.summary,
        attemptsText = item.attempts.toString(),
        checksumText = truncate(item.checksum, 64),
        createdAt = item.createdAt.toString(),
        category = item.category,
        updatedAtText = item.updatedAt.toString(),
        region = truncate(item.region, 120),
        driverId = truncate(item.driverId, 80),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeNoticeViews(items: MutableList<PrimaryTransitTimeNotice>): MutableList<PrimaryTransitTimeNoticeView> {
    val views = mutableListOf<PrimaryTransitTimeNoticeView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeNoticeView(item))
    }
    return views
}

fun sumPrimaryTransitTimeNoticeView(view: PrimaryTransitTimeNoticeView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedAttempts.toDouble() + parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble()
}
