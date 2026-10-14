package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff draft. */
data class TariffDraftView(
    var codeText: String,
    var name: String,
    var capacity: String,
    var minimum: String,
    var ceilingText: String,
    var stopCountText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toTariffDraftView(item: TariffDraft): TariffDraftView {
    return TariffDraftView(
        codeText = item.code,
        name = truncate(item.name, 64),
        capacity = item.capacity.toString(),
        minimum = "%.2f".format(item.minimum),
        ceilingText = "%.2f".format(item.ceiling),
        stopCountText = item.stopCount.toString(),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffDraftViews(items: MutableList<TariffDraft>): MutableList<TariffDraftView> {
    val views = mutableListOf<TariffDraftView>()
    for (item in items) {
        views.add(toTariffDraftView(item))
    }
    return views
}

fun sumTariffDraftView(view: TariffDraftView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedMinimum = view.minimum.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedCapacity.toDouble() + parsedMinimum + parsedCeiling
}
