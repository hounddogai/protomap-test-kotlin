package com.acme.transport.lastmile.drop.rollup

/** How screens and exports show a regional drop tier. */
data class RegionalDropTierView(
    var code: String,
    var titleText: String,
    var contactEmailText: String,
    var locale: String,
    var channel: String,
    var batchSize: String,
    var palletsText: String,
    var ownerIdText: String,
    var expiresAt: String,
    var etaMinutes: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toRegionalDropTierView(item: RegionalDropTier): RegionalDropTierView {
    return RegionalDropTierView(
        code = item.code,
        titleText = item.title,
        contactEmailText = item.contactEmail,
        locale = item.locale,
        channel = item.channel,
        batchSize = item.batchSize.toString(),
        palletsText = item.pallets.toString(),
        ownerIdText = item.ownerId,
        expiresAt = item.expiresAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDropTierViews(items: MutableList<RegionalDropTier>): MutableList<RegionalDropTierView> {
    val views = mutableListOf<RegionalDropTierView>()
    for (item in items) {
        views.add(toRegionalDropTierView(item))
    }
    return views
}

fun sumRegionalDropTierView(view: RegionalDropTierView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedBatchSize.toDouble() + parsedPallets.toDouble() + parsedExpiresAt.toDouble()
}
