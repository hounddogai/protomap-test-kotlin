package com.acme.transport.freightpay.audit.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global audit claim. */
data class GlobalAuditClaimView(
    var code: String,
    var label: String,
    var notes: String,
    var attempts: String,
    var channelText: String,
    var countryText: String,
    var visible: String,
    var etaMinutesText: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toGlobalAuditClaimView(item: GlobalAuditClaim): GlobalAuditClaimView {
    return GlobalAuditClaimView(
        code = truncate(item.code, 40),
        label = truncate(item.label, 64),
        notes = item.notes,
        attempts = item.attempts.toString(),
        channelText = item.channel,
        countryText = item.country,
        visible = "${item.visible}",
        etaMinutesText = item.etaMinutes.toString(),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAuditClaimViews(items: MutableList<GlobalAuditClaim>): MutableList<GlobalAuditClaimView> {
    val views = mutableListOf<GlobalAuditClaimView>()
    for (item in items) {
        views.add(toGlobalAuditClaimView(item))
    }
    return views
}

fun sumGlobalAuditClaimView(view: GlobalAuditClaimView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedAttempts.toDouble() + parsedEtaMinutes.toDouble() + parsedStartsAt.toDouble()
}
