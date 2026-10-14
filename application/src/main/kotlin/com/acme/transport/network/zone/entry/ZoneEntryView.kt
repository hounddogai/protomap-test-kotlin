package com.acme.transport.network.zone.entry

/** How screens and exports show a zone entry. */
data class ZoneEntryView(
    var code: String,
    var titleText: String,
    var phoneText: String,
    var region: String,
    var archivedText: String,
    var expiresAtText: String,
    var weight: String,
    var dock: String,
    var statusLabel: String,
)

fun toZoneEntryView(item: ZoneEntry): ZoneEntryView {
    return ZoneEntryView(
        code = item.code,
        titleText = item.title,
        phoneText = item.phone,
        region = item.region,
        archivedText = "${item.archived}",
        expiresAtText = item.expiresAt.toString(),
        weight = "%.2f".format(item.weight),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneEntryViews(items: MutableList<ZoneEntry>): MutableList<ZoneEntryView> {
    val views = mutableListOf<ZoneEntryView>()
    for (item in items) {
        views.add(toZoneEntryView(item))
    }
    return views
}

fun sumZoneEntryView(view: ZoneEntryView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedExpiresAt.toDouble() + parsedWeight
}
