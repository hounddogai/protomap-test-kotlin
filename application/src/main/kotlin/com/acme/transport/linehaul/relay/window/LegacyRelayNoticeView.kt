package com.acme.transport.linehaul.relay.window

/** How screens and exports show a legacy relay notice. */
data class LegacyRelayNoticeView(
    var code: String,
    var titleText: String,
    var expiresAt: String,
    var amountText: String,
    var threshold: String,
    var trailerId: String,
    var timezone: String,
    var locale: String,
    var startsAt: String,
    var phone: String,
    var statusLabel: String,
)

fun toLegacyRelayNoticeView(item: LegacyRelayNotice): LegacyRelayNoticeView {
    return LegacyRelayNoticeView(
        code = item.code,
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        amountText = "%.2f".format(item.amount),
        threshold = "%.2f".format(item.threshold),
        trailerId = item.trailerId,
        timezone = item.timezone,
        locale = item.locale,
        startsAt = item.startsAt.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRelayNoticeViews(items: MutableList<LegacyRelayNotice>): MutableList<LegacyRelayNoticeView> {
    val views = mutableListOf<LegacyRelayNoticeView>()
    for (item in items) {
        views.add(toLegacyRelayNoticeView(item))
    }
    return views
}

fun sumLegacyRelayNoticeView(view: LegacyRelayNoticeView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedAmount = view.amountText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedExpiresAt.toDouble() + parsedAmount + parsedThreshold
}
