package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default hub mapping. */
data class DefaultHubMappingView(
    var code: String,
    var title: String,
    var regionText: String,
    var durationMinutes: String,
    var visible: String,
    var endsAt: String,
    var pinnedText: String,
    var reviewerText: String,
    var channel: String,
    var segment: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toDefaultHubMappingView(item: DefaultHubMapping): DefaultHubMappingView {
    return DefaultHubMappingView(
        code = item.code,
        title = item.title,
        regionText = item.region,
        durationMinutes = item.durationMinutes.toString(),
        visible = "${item.visible}",
        endsAt = item.endsAt.toString(),
        pinnedText = "${item.pinned}",
        reviewerText = item.reviewer,
        channel = truncate(item.channel, 64),
        segment = item.segment,
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHubMappingViews(items: MutableList<DefaultHubMapping>): MutableList<DefaultHubMappingView> {
    val views = mutableListOf<DefaultHubMappingView>()
    for (item in items) {
        views.add(toDefaultHubMappingView(item))
    }
    return views
}

fun sumDefaultHubMappingView(view: DefaultHubMappingView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedDurationMinutes.toDouble() + parsedEndsAt.toDouble()
}
