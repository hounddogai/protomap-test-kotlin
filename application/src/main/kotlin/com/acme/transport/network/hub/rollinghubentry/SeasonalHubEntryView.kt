package com.acme.transport.network.hub.rollinghubentry

/** How screens and exports show a seasonal hub entry. */
data class SeasonalHubEntryView(
    var codeText: String,
    var name: String,
    var budget: String,
    var countryText: String,
    var capacity: String,
    var locale: String,
    var statusLabel: String,
)

fun toSeasonalHubEntryView(item: SeasonalHubEntry): SeasonalHubEntryView {
    return SeasonalHubEntryView(
        codeText = item.code,
        name = item.name,
        budget = "%.2f".format(item.budget),
        countryText = item.country,
        capacity = item.capacity.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalHubEntryViews(items: MutableList<SeasonalHubEntry>): MutableList<SeasonalHubEntryView> {
    val views = mutableListOf<SeasonalHubEntryView>()
    for (item in items) {
        views.add(toSeasonalHubEntryView(item))
    }
    return views
}

fun sumSeasonalHubEntryView(view: SeasonalHubEntryView): Double {
    val parsedBudget = view.budget.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedBudget + parsedCapacity.toDouble()
}
