package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff target. */
data class TariffTargetView(
    var codeText: String,
    var title: String,
    var timezone: String,
    var routeCode: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toTariffTargetView(item: TariffTarget): TariffTargetView {
    return TariffTargetView(
        codeText = item.code,
        title = truncate(item.title, 120),
        timezone = truncate(item.timezone, 120),
        routeCode = truncate(item.routeCode, 64),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffTargetViews(items: MutableList<TariffTarget>): MutableList<TariffTargetView> {
    val views = mutableListOf<TariffTargetView>()
    for (item in items) {
        views.add(toTariffTargetView(item))
    }
    return views
}

fun sumTariffTargetView(view: TariffTargetView): Double {
    val parsedScore = view.scoreText.toDouble()
    return parsedScore
}
