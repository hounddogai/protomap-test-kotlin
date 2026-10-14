package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy declaration summary. */
data class LegacyDeclarationSummaryView(
    var code: String,
    var nameText: String,
    var cost: String,
    var phone: String,
    var notes: String,
    var vehicleId: String,
    var contactEmail: String,
    var channel: String,
    var statusLabel: String,
)

fun toLegacyDeclarationSummaryView(item: LegacyDeclarationSummary): LegacyDeclarationSummaryView {
    return LegacyDeclarationSummaryView(
        code = item.code,
        nameText = item.name,
        cost = "%.2f".format(item.cost),
        phone = item.phone,
        notes = item.notes,
        vehicleId = truncate(item.vehicleId, 80),
        contactEmail = item.contactEmail,
        channel = truncate(item.channel, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDeclarationSummaryViews(items: MutableList<LegacyDeclarationSummary>): MutableList<LegacyDeclarationSummaryView> {
    val views = mutableListOf<LegacyDeclarationSummaryView>()
    for (item in items) {
        views.add(toLegacyDeclarationSummaryView(item))
    }
    return views
}

fun sumLegacyDeclarationSummaryView(view: LegacyDeclarationSummaryView): Double {
    val parsedCost = view.cost.toDouble()
    return parsedCost
}
