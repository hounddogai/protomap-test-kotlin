package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling backhaul template. */
data class RollingBackhaulTemplateView(
    var code: String,
    var name: String,
    var timezoneText: String,
    var scoreText: String,
    var capacity: String,
    var locale: String,
    var statusLabel: String,
)

fun toRollingBackhaulTemplateView(item: RollingBackhaulTemplate): RollingBackhaulTemplateView {
    return RollingBackhaulTemplateView(
        code = truncate(item.code, 64),
        name = item.name,
        timezoneText = truncate(item.timezone, 40),
        scoreText = "%.2f".format(item.score),
        capacity = item.capacity.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingBackhaulTemplateViews(items: MutableList<RollingBackhaulTemplate>): MutableList<RollingBackhaulTemplateView> {
    val views = mutableListOf<RollingBackhaulTemplateView>()
    for (item in items) {
        views.add(toRollingBackhaulTemplateView(item))
    }
    return views
}

fun sumRollingBackhaulTemplateView(view: RollingBackhaulTemplateView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedScore + parsedCapacity.toDouble()
}
