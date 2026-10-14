package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped idling target. */
data class CappedIdlingTargetView(
    var code: String,
    var labelText: String,
    var timezone: String,
    var createdAt: String,
    var approved: String,
    var region: String,
    var loadKg: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toCappedIdlingTargetView(item: CappedIdlingTarget): CappedIdlingTargetView {
    return CappedIdlingTargetView(
        code = item.code,
        labelText = truncate(item.label, 64),
        timezone = truncate(item.timezone, 80),
        createdAt = item.createdAt.toString(),
        approved = "${item.approved}",
        region = item.region,
        loadKg = "%.2f".format(item.loadKg),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIdlingTargetViews(items: MutableList<CappedIdlingTarget>): MutableList<CappedIdlingTargetView> {
    val views = mutableListOf<CappedIdlingTargetView>()
    for (item in items) {
        views.add(toCappedIdlingTargetView(item))
    }
    return views
}

fun sumCappedIdlingTargetView(view: CappedIdlingTargetView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCreatedAt.toDouble() + parsedLoadKg + parsedExpiresAt.toDouble()
}
