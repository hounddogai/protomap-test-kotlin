package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual hub policy. */
data class ManualHubPolicyView(
    var codeText: String,
    var nameText: String,
    var routeCodeText: String,
    var dock: String,
    var endsAtText: String,
    var currencyText: String,
    var threshold: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toManualHubPolicyView(item: ManualHubPolicy): ManualHubPolicyView {
    return ManualHubPolicyView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        routeCodeText = item.routeCode,
        dock = item.dock,
        endsAtText = item.endsAt.toString(),
        currencyText = item.currency,
        threshold = "%.2f".format(item.threshold),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualHubPolicyViews(items: MutableList<ManualHubPolicy>): MutableList<ManualHubPolicyView> {
    val views = mutableListOf<ManualHubPolicyView>()
    for (item in items) {
        views.add(toManualHubPolicyView(item))
    }
    return views
}

fun sumManualHubPolicyView(view: ManualHubPolicyView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedEndsAt.toDouble() + parsedThreshold + parsedWindowDays.toDouble()
}
