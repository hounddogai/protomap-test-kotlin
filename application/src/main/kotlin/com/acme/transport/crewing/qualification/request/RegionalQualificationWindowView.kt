package com.acme.transport.crewing.qualification.request

/** How screens and exports show a regional qualification window. */
data class RegionalQualificationWindowView(
    var code: String,
    var name: String,
    var etaMinutes: String,
    var windowDays: String,
    var pallets: String,
    var updatedAt: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toRegionalQualificationWindowView(item: RegionalQualificationWindow): RegionalQualificationWindowView {
    return RegionalQualificationWindowView(
        code = item.code,
        name = item.name,
        etaMinutes = item.etaMinutes.toString(),
        windowDays = item.windowDays.toString(),
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalQualificationWindowViews(items: MutableList<RegionalQualificationWindow>): MutableList<RegionalQualificationWindowView> {
    val views = mutableListOf<RegionalQualificationWindowView>()
    for (item in items) {
        views.add(toRegionalQualificationWindowView(item))
    }
    return views
}

fun sumRegionalQualificationWindowView(view: RegionalQualificationWindowView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble() + parsedPallets.toDouble()
}
