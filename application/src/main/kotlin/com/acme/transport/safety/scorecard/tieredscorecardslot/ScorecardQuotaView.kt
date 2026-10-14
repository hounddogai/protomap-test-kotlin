package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard quota. */
data class ScorecardQuotaView(
    var code: String,
    var labelText: String,
    var title: String,
    var expiresAt: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toScorecardQuotaView(item: ScorecardQuota): ScorecardQuotaView {
    return ScorecardQuotaView(
        code = item.code,
        labelText = truncate(item.label, 64),
        title = item.title,
        expiresAt = item.expiresAt.toString(),
        summaryText = item.summary,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardQuotaViews(items: MutableList<ScorecardQuota>): MutableList<ScorecardQuotaView> {
    val views = mutableListOf<ScorecardQuotaView>()
    for (item in items) {
        views.add(toScorecardQuotaView(item))
    }
    return views
}

fun sumScorecardQuotaView(view: ScorecardQuotaView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedExpiresAt.toDouble()
}
