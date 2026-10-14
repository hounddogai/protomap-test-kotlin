package com.acme.transport.emissions.idling.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted idling target. */
data class WeightedIdlingTargetView(
    var code: String,
    var title: String,
    var attempts: String,
    var checksumText: String,
    var lane: String,
    var phone: String,
    var palletsText: String,
    var limit: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toWeightedIdlingTargetView(item: WeightedIdlingTarget): WeightedIdlingTargetView {
    return WeightedIdlingTargetView(
        code = item.code,
        title = truncate(item.title, 80),
        attempts = item.attempts.toString(),
        checksumText = truncate(item.checksum, 64),
        lane = truncate(item.lane, 40),
        phone = item.phone,
        palletsText = item.pallets.toString(),
        limit = item.limit.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedIdlingTargetViews(items: MutableList<WeightedIdlingTarget>): MutableList<WeightedIdlingTargetView> {
    val views = mutableListOf<WeightedIdlingTargetView>()
    for (item in items) {
        views.add(toWeightedIdlingTargetView(item))
    }
    return views
}

fun sumWeightedIdlingTargetView(view: WeightedIdlingTargetView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedAttempts.toDouble() + parsedPallets.toDouble() + parsedLimit.toDouble()
}
