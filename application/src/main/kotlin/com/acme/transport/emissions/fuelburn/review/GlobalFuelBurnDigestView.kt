package com.acme.transport.emissions.fuelburn.review

/** How screens and exports show a global fuel burn digest. */
data class GlobalFuelBurnDigestView(
    var code: String,
    var label: String,
    var description: String,
    var externalRef: String,
    var costText: String,
    var active: String,
    var trailerId: String,
    var archived: String,
    var statusLabel: String,
)

fun toGlobalFuelBurnDigestView(item: GlobalFuelBurnDigest): GlobalFuelBurnDigestView {
    return GlobalFuelBurnDigestView(
        code = item.code,
        label = item.label,
        description = item.description,
        externalRef = item.externalRef,
        costText = "%.2f".format(item.cost),
        active = "${item.active}",
        trailerId = item.trailerId,
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalFuelBurnDigestViews(items: MutableList<GlobalFuelBurnDigest>): MutableList<GlobalFuelBurnDigestView> {
    val views = mutableListOf<GlobalFuelBurnDigestView>()
    for (item in items) {
        views.add(toGlobalFuelBurnDigestView(item))
    }
    return views
}

fun sumGlobalFuelBurnDigestView(view: GlobalFuelBurnDigestView): Double {
    val parsedCost = view.costText.toDouble()
    return parsedCost
}
