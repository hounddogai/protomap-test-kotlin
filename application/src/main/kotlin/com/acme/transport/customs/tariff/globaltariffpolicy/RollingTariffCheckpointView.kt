package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling tariff checkpoint. */
data class RollingTariffCheckpointView(
    var codeText: String,
    var titleText: String,
    var region: String,
    var sourceText: String,
    var visibleText: String,
    var limitText: String,
    var countryText: String,
    var laneText: String,
    var retriesText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toRollingTariffCheckpointView(item: RollingTariffCheckpoint): RollingTariffCheckpointView {
    return RollingTariffCheckpointView(
        codeText = item.code,
        titleText = item.title,
        region = item.region,
        sourceText = truncate(item.source, 40),
        visibleText = "${item.visible}",
        limitText = item.limit.toString(),
        countryText = item.country,
        laneText = item.lane,
        retriesText = item.retries.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTariffCheckpointViews(items: MutableList<RollingTariffCheckpoint>): MutableList<RollingTariffCheckpointView> {
    val views = mutableListOf<RollingTariffCheckpointView>()
    for (item in items) {
        views.add(toRollingTariffCheckpointView(item))
    }
    return views
}

fun sumRollingTariffCheckpointView(view: RollingTariffCheckpointView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedRetries = view.retriesText.toInt()
    return parsedLimit.toDouble() + parsedRetries.toDouble()
}
