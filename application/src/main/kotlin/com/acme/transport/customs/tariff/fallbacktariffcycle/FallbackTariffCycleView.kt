package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tariff cycle. */
data class FallbackTariffCycleView(
    var code: String,
    var title: String,
    var currency: String,
    var contactEmail: String,
    var timezone: String,
    var approved: String,
    var phoneText: String,
    var stopCountText: String,
    var sourceText: String,
    var statusLabel: String,
)

fun toFallbackTariffCycleView(item: FallbackTariffCycle): FallbackTariffCycleView {
    return FallbackTariffCycleView(
        code = truncate(item.code, 40),
        title = item.title,
        currency = truncate(item.currency, 40),
        contactEmail = item.contactEmail,
        timezone = item.timezone,
        approved = "${item.approved}",
        phoneText = item.phone,
        stopCountText = item.stopCount.toString(),
        sourceText = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTariffCycleViews(items: MutableList<FallbackTariffCycle>): MutableList<FallbackTariffCycleView> {
    val views = mutableListOf<FallbackTariffCycleView>()
    for (item in items) {
        views.add(toFallbackTariffCycleView(item))
    }
    return views
}

fun sumFallbackTariffCycleView(view: FallbackTariffCycleView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    return parsedStopCount.toDouble()
}
