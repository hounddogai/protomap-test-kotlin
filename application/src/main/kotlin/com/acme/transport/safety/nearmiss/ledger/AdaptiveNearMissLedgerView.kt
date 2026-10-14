package com.acme.transport.safety.nearmiss.ledger

/** How screens and exports show a adaptive near miss ledger. */
data class AdaptiveNearMissLedgerView(
    var code: String,
    var label: String,
    var etaMinutes: String,
    var expiresAt: String,
    var graceDays: String,
    var trailerId: String,
    var visibleText: String,
    var locale: String,
    var statusLabel: String,
)

fun toAdaptiveNearMissLedgerView(item: AdaptiveNearMissLedger): AdaptiveNearMissLedgerView {
    return AdaptiveNearMissLedgerView(
        code = item.code,
        label = item.label,
        etaMinutes = item.etaMinutes.toString(),
        expiresAt = item.expiresAt.toString(),
        graceDays = item.graceDays.toString(),
        trailerId = item.trailerId,
        visibleText = "${item.visible}",
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveNearMissLedgerViews(items: MutableList<AdaptiveNearMissLedger>): MutableList<AdaptiveNearMissLedgerView> {
    val views = mutableListOf<AdaptiveNearMissLedgerView>()
    for (item in items) {
        views.add(toAdaptiveNearMissLedgerView(item))
    }
    return views
}

fun sumAdaptiveNearMissLedgerView(view: AdaptiveNearMissLedgerView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedEtaMinutes.toDouble() + parsedExpiresAt.toDouble() + parsedGraceDays.toDouble()
}
