package com.acme.transport.emissions.evcharge.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global ev charge plan. */
data class GlobalEvChargePlanView(
    var codeText: String,
    var name: String,
    var carrierText: String,
    var dock: String,
    var score: String,
    var version: String,
    var trailerId: String,
    var limitText: String,
    var discount: String,
    var maximum: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toGlobalEvChargePlanView(item: GlobalEvChargePlan): GlobalEvChargePlanView {
    return GlobalEvChargePlanView(
        codeText = item.code,
        name = item.name,
        carrierText = truncate(item.carrier, 64),
        dock = truncate(item.dock, 40),
        score = "%.2f".format(item.score),
        version = item.version.toString(),
        trailerId = truncate(item.trailerId, 64),
        limitText = item.limit.toString(),
        discount = "%.2f".format(item.discount),
        maximum = "%.2f".format(item.maximum),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalEvChargePlanViews(items: MutableList<GlobalEvChargePlan>): MutableList<GlobalEvChargePlanView> {
    val views = mutableListOf<GlobalEvChargePlanView>()
    for (item in items) {
        views.add(toGlobalEvChargePlanView(item))
    }
    return views
}

fun sumGlobalEvChargePlanView(view: GlobalEvChargePlanView): Double {
    val parsedScore = view.score.toDouble()
    val parsedVersion = view.version.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedScore + parsedVersion.toDouble() + parsedLimit.toDouble()
}
