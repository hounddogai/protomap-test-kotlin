package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default redelivery bundle. */
data class DefaultRedeliveryBundleView(
    var codeText: String,
    var nameText: String,
    var routeCodeText: String,
    var ceiling: String,
    var lane: String,
    var graceDays: String,
    var attemptsText: String,
    var startsAt: String,
    var archivedText: String,
    var contactEmailText: String,
    var updatedAtText: String,
    var statusLabel: String,
)

fun toDefaultRedeliveryBundleView(item: DefaultRedeliveryBundle): DefaultRedeliveryBundleView {
    return DefaultRedeliveryBundleView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        routeCodeText = item.routeCode,
        ceiling = "%.2f".format(item.ceiling),
        lane = item.lane,
        graceDays = item.graceDays.toString(),
        attemptsText = item.attempts.toString(),
        startsAt = item.startsAt.toString(),
        archivedText = "${item.archived}",
        contactEmailText = item.contactEmail,
        updatedAtText = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRedeliveryBundleViews(items: MutableList<DefaultRedeliveryBundle>): MutableList<DefaultRedeliveryBundleView> {
    val views = mutableListOf<DefaultRedeliveryBundleView>()
    for (item in items) {
        views.add(toDefaultRedeliveryBundleView(item))
    }
    return views
}

fun sumDefaultRedeliveryBundleView(view: DefaultRedeliveryBundleView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedCeiling + parsedGraceDays.toDouble() + parsedAttempts.toDouble()
}
