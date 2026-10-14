package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scorecard profile. */
data class ScorecardProfileView(
    var code: String,
    var labelText: String,
    var updatedAt: String,
    var expiresAt: String,
    var maximum: String,
    var palletsText: String,
    var phone: String,
    var name: String,
    var statusLabel: String,
)

fun toScorecardProfileView(item: ScorecardProfile): ScorecardProfileView {
    return ScorecardProfileView(
        code = item.code,
        labelText = truncate(item.label, 40),
        updatedAt = item.updatedAt.toString(),
        expiresAt = item.expiresAt.toString(),
        maximum = "%.2f".format(item.maximum),
        palletsText = item.pallets.toString(),
        phone = item.phone,
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardProfileViews(items: MutableList<ScorecardProfile>): MutableList<ScorecardProfileView> {
    val views = mutableListOf<ScorecardProfileView>()
    for (item in items) {
        views.add(toScorecardProfileView(item))
    }
    return views
}

fun sumScorecardProfileView(view: ScorecardProfileView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedUpdatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedMaximum
}
