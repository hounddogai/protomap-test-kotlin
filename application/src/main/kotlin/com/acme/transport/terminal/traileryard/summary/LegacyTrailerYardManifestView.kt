package com.acme.transport.terminal.traileryard.summary

/** How screens and exports show a legacy trailer yard manifest. */
data class LegacyTrailerYardManifestView(
    var codeText: String,
    var nameText: String,
    var contactEmailText: String,
    var active: String,
    var amount: String,
    var windowDaysText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toLegacyTrailerYardManifestView(item: LegacyTrailerYardManifest): LegacyTrailerYardManifestView {
    return LegacyTrailerYardManifestView(
        codeText = item.code,
        nameText = item.name,
        contactEmailText = item.contactEmail,
        active = "${item.active}",
        amount = "%.2f".format(item.amount),
        windowDaysText = item.windowDays.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTrailerYardManifestViews(items: MutableList<LegacyTrailerYardManifest>): MutableList<LegacyTrailerYardManifestView> {
    val views = mutableListOf<LegacyTrailerYardManifestView>()
    for (item in items) {
        views.add(toLegacyTrailerYardManifestView(item))
    }
    return views
}

fun sumLegacyTrailerYardManifestView(view: LegacyTrailerYardManifestView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    return parsedAmount + parsedWindowDays.toDouble() + parsedRatio
}
