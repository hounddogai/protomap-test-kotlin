package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a roster claim. */
data class RosterClaimView(
    var code: String,
    var name: String,
    var currency: String,
    var vehicleId: String,
    var externalRef: String,
    var amount: String,
    var statusLabel: String,
)

fun toRosterClaimView(item: RosterClaim): RosterClaimView {
    return RosterClaimView(
        code = truncate(item.code, 80),
        name = item.name,
        currency = truncate(item.currency, 40),
        vehicleId = truncate(item.vehicleId, 64),
        externalRef = truncate(item.externalRef, 120),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRosterClaimViews(items: MutableList<RosterClaim>): MutableList<RosterClaimView> {
    val views = mutableListOf<RosterClaimView>()
    for (item in items) {
        views.add(toRosterClaimView(item))
    }
    return views
}

fun sumRosterClaimView(view: RosterClaimView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
