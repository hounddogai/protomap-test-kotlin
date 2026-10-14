package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global hub summary. */
data class GlobalHubSummaryView(
    var code: String,
    var name: String,
    var pallets: String,
    var threshold: String,
    var weightText: String,
    var retries: String,
    var version: String,
    var fuelLitersText: String,
    var description: String,
    var statusLabel: String,
)

fun toGlobalHubSummaryView(item: GlobalHubSummary): GlobalHubSummaryView {
    return GlobalHubSummaryView(
        code = item.code,
        name = item.name,
        pallets = item.pallets.toString(),
        threshold = "%.2f".format(item.threshold),
        weightText = "%.2f".format(item.weight),
        retries = item.retries.toString(),
        version = item.version.toString(),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        description = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalHubSummaryViews(items: MutableList<GlobalHubSummary>): MutableList<GlobalHubSummaryView> {
    val views = mutableListOf<GlobalHubSummaryView>()
    for (item in items) {
        views.add(toGlobalHubSummaryView(item))
    }
    return views
}

fun sumGlobalHubSummaryView(view: GlobalHubSummaryView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedThreshold = view.threshold.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedPallets.toDouble() + parsedThreshold + parsedWeight
}
