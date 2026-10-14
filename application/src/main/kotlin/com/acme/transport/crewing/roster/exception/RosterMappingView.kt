package com.acme.transport.crewing.roster.exception

/** How screens and exports show a roster mapping. */
data class RosterMappingView(
    var code: String,
    var label: String,
    var routeCodeText: String,
    var externalRef: String,
    var palletsText: String,
    var statusLabel: String,
)

fun toRosterMappingView(item: RosterMapping): RosterMappingView {
    return RosterMappingView(
        code = item.code,
        label = item.label,
        routeCodeText = item.routeCode,
        externalRef = item.externalRef,
        palletsText = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterMappingViews(items: MutableList<RosterMapping>): MutableList<RosterMappingView> {
    val views = mutableListOf<RosterMappingView>()
    for (item in items) {
        views.add(toRosterMappingView(item))
    }
    return views
}

fun sumRosterMappingView(view: RosterMappingView): Double {
    val parsedPallets = view.palletsText.toInt()
    return parsedPallets.toDouble()
}
