package com.acme.transport.safety.coaching.digest

/** How screens and exports show a default coaching baseline. */
data class DefaultCoachingBaselineView(
    var code: String,
    var name: String,
    var pallets: String,
    var timezoneText: String,
    var phone: String,
    var etaMinutes: String,
    var minimumText: String,
    var thresholdText: String,
    var windowDays: String,
    var durationMinutes: String,
    var visible: String,
    var statusLabel: String,
)

fun toDefaultCoachingBaselineView(item: DefaultCoachingBaseline): DefaultCoachingBaselineView {
    return DefaultCoachingBaselineView(
        code = item.code,
        name = item.name,
        pallets = item.pallets.toString(),
        timezoneText = item.timezone,
        phone = item.phone,
        etaMinutes = item.etaMinutes.toString(),
        minimumText = "%.2f".format(item.minimum),
        thresholdText = "%.2f".format(item.threshold),
        windowDays = item.windowDays.toString(),
        durationMinutes = item.durationMinutes.toString(),
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCoachingBaselineViews(items: MutableList<DefaultCoachingBaseline>): MutableList<DefaultCoachingBaselineView> {
    val views = mutableListOf<DefaultCoachingBaselineView>()
    for (item in items) {
        views.add(toDefaultCoachingBaselineView(item))
    }
    return views
}

fun sumDefaultCoachingBaselineView(view: DefaultCoachingBaselineView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedPallets.toDouble() + parsedEtaMinutes.toDouble() + parsedMinimum
}
