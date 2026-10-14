package com.acme.transport.network.node.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling node score. */
data class RollingNodeScoreView(
    var codeText: String,
    var title: String,
    var etaMinutesText: String,
    var locale: String,
    var currency: String,
    var phone: String,
    var timezone: String,
    var unit: String,
    var activeText: String,
    var statusLabel: String,
)

fun toRollingNodeScoreView(item: RollingNodeScore): RollingNodeScoreView {
    return RollingNodeScoreView(
        codeText = item.code,
        title = item.title,
        etaMinutesText = item.etaMinutes.toString(),
        locale = truncate(item.locale, 64),
        currency = item.currency,
        phone = truncate(item.phone, 120),
        timezone = item.timezone,
        unit = item.unit,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingNodeScoreViews(items: MutableList<RollingNodeScore>): MutableList<RollingNodeScoreView> {
    val views = mutableListOf<RollingNodeScoreView>()
    for (item in items) {
        views.add(toRollingNodeScoreView(item))
    }
    return views
}

fun sumRollingNodeScoreView(view: RollingNodeScoreView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedEtaMinutes.toDouble()
}
