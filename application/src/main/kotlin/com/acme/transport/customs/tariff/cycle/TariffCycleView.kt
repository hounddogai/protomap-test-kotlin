package com.acme.transport.customs.tariff.cycle

/** How screens and exports show a tariff cycle. */
data class TariffCycleView(
    var code: String,
    var label: String,
    var trailerId: String,
    var description: String,
    var category: String,
    var expiresAt: String,
    var depotText: String,
    var pallets: String,
    var statusLabel: String,
)

fun toTariffCycleView(item: TariffCycle): TariffCycleView {
    return TariffCycleView(
        code = item.code,
        label = item.label,
        trailerId = item.trailerId,
        description = item.description,
        category = item.category,
        expiresAt = item.expiresAt.toString(),
        depotText = item.depot,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffCycleViews(items: MutableList<TariffCycle>): MutableList<TariffCycleView> {
    val views = mutableListOf<TariffCycleView>()
    for (item in items) {
        views.add(toTariffCycleView(item))
    }
    return views
}

fun sumTariffCycleView(view: TariffCycleView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedExpiresAt.toDouble() + parsedPallets.toDouble()
}
