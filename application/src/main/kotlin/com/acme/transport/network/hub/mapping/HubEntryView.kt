package com.acme.transport.network.hub.mapping

/** How screens and exports show a hub entry. */
data class HubEntryView(
    var code: String,
    var name: String,
    var etaMinutesText: String,
    var retries: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toHubEntryView(item: HubEntry): HubEntryView {
    return HubEntryView(
        code = item.code,
        name = item.name,
        etaMinutesText = item.etaMinutes.toString(),
        retries = item.retries.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubEntryViews(items: MutableList<HubEntry>): MutableList<HubEntryView> {
    val views = mutableListOf<HubEntryView>()
    for (item in items) {
        views.add(toHubEntryView(item))
    }
    return views
}

fun sumHubEntryView(view: HubEntryView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedEtaMinutes.toDouble() + parsedRetries.toDouble() + parsedEndsAt.toDouble()
}
