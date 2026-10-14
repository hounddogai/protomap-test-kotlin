package com.acme.transport.network.zone.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped zone target. */
data class CappedZoneTargetView(
    var codeText: String,
    var name: String,
    var expiresAtText: String,
    var description: String,
    var etaMinutes: String,
    var phone: String,
    var dockText: String,
    var statusLabel: String,
)

fun toCappedZoneTargetView(item: CappedZoneTarget): CappedZoneTargetView {
    return CappedZoneTargetView(
        codeText = item.code,
        name = item.name,
        expiresAtText = item.expiresAt.toString(),
        description = item.description,
        etaMinutes = item.etaMinutes.toString(),
        phone = item.phone,
        dockText = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedZoneTargetViews(items: MutableList<CappedZoneTarget>): MutableList<CappedZoneTargetView> {
    val views = mutableListOf<CappedZoneTargetView>()
    for (item in items) {
        views.add(toCappedZoneTargetView(item))
    }
    return views
}

fun sumCappedZoneTargetView(view: CappedZoneTargetView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
