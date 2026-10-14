package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default relay template. */
data class DefaultRelayTemplateView(
    var codeText: String,
    var label: String,
    var ownerIdText: String,
    var rateText: String,
    var segment: String,
    var statusLabel: String,
)

fun toDefaultRelayTemplateView(item: DefaultRelayTemplate): DefaultRelayTemplateView {
    return DefaultRelayTemplateView(
        codeText = truncate(item.code, 40),
        label = item.label,
        ownerIdText = item.ownerId,
        rateText = "%.2f".format(item.rate),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRelayTemplateViews(items: MutableList<DefaultRelayTemplate>): MutableList<DefaultRelayTemplateView> {
    val views = mutableListOf<DefaultRelayTemplateView>()
    for (item in items) {
        views.add(toDefaultRelayTemplateView(item))
    }
    return views
}

fun sumDefaultRelayTemplateView(view: DefaultRelayTemplateView): Double {
    val parsedRate = view.rateText.toDouble()
    return parsedRate
}
