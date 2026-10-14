package com.acme.transport.network.cutoff.globalcutoffledger

/** How screens and exports show a fallback cutoff claim. */
data class FallbackCutoffClaimView(
    var code: String,
    var label: String,
    var revision: String,
    var driverIdText: String,
    var locale: String,
    var costText: String,
    var description: String,
    var contactEmail: String,
    var vehicleId: String,
    var active: String,
    var statusLabel: String,
)

fun toFallbackCutoffClaimView(item: FallbackCutoffClaim): FallbackCutoffClaimView {
    return FallbackCutoffClaimView(
        code = item.code,
        label = item.label,
        revision = item.revision.toString(),
        driverIdText = item.driverId,
        locale = item.locale,
        costText = "%.2f".format(item.cost),
        description = item.description,
        contactEmail = item.contactEmail,
        vehicleId = item.vehicleId,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackCutoffClaimViews(items: MutableList<FallbackCutoffClaim>): MutableList<FallbackCutoffClaimView> {
    val views = mutableListOf<FallbackCutoffClaimView>()
    for (item in items) {
        views.add(toFallbackCutoffClaimView(item))
    }
    return views
}

fun sumFallbackCutoffClaimView(view: FallbackCutoffClaimView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedRevision.toDouble() + parsedCost
}
