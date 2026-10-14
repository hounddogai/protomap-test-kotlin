package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global broker override. */
data class GlobalBrokerOverrideView(
    var codeText: String,
    var titleText: String,
    var startsAtText: String,
    var source: String,
    var archived: String,
    var locale: String,
    var margin: String,
    var statusLabel: String,
)

fun toGlobalBrokerOverrideView(item: GlobalBrokerOverride): GlobalBrokerOverrideView {
    return GlobalBrokerOverrideView(
        codeText = truncate(item.code, 120),
        titleText = item.title,
        startsAtText = item.startsAt.toString(),
        source = truncate(item.source, 80),
        archived = "${item.archived}",
        locale = item.locale,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBrokerOverrideViews(items: MutableList<GlobalBrokerOverride>): MutableList<GlobalBrokerOverrideView> {
    val views = mutableListOf<GlobalBrokerOverrideView>()
    for (item in items) {
        views.add(toGlobalBrokerOverrideView(item))
    }
    return views
}

fun sumGlobalBrokerOverrideView(view: GlobalBrokerOverrideView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedStartsAt.toDouble() + parsedMargin
}
