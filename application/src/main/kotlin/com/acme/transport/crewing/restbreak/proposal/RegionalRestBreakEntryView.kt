package com.acme.transport.crewing.restbreak.proposal

/** How screens and exports show a regional rest break entry. */
data class RegionalRestBreakEntryView(
    var codeText: String,
    var name: String,
    var attempts: String,
    var endsAt: String,
    var visible: String,
    var etaMinutes: String,
    var pinned: String,
    var vehicleIdText: String,
    var lane: String,
    var maximum: String,
    var statusLabel: String,
)

fun toRegionalRestBreakEntryView(item: RegionalRestBreakEntry): RegionalRestBreakEntryView {
    return RegionalRestBreakEntryView(
        codeText = item.code,
        name = item.name,
        attempts = item.attempts.toString(),
        endsAt = item.endsAt.toString(),
        visible = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        pinned = "${item.pinned}",
        vehicleIdText = item.vehicleId,
        lane = item.lane,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRestBreakEntryViews(items: MutableList<RegionalRestBreakEntry>): MutableList<RegionalRestBreakEntryView> {
    val views = mutableListOf<RegionalRestBreakEntryView>()
    for (item in items) {
        views.add(toRegionalRestBreakEntryView(item))
    }
    return views
}

fun sumRegionalRestBreakEntryView(view: RegionalRestBreakEntryView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedAttempts.toDouble() + parsedEndsAt.toDouble() + parsedEtaMinutes.toDouble()
}
