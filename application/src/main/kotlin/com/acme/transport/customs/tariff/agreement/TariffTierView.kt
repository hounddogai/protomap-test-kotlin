package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff tier. */
data class TariffTierView(
    var code: String,
    var titleText: String,
    var depot: String,
    var revisionText: String,
    var etaMinutes: String,
    var budget: String,
    var minimum: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toTariffTierView(item: TariffTier): TariffTierView {
    return TariffTierView(
        code = item.code,
        titleText = truncate(item.title, 64),
        depot = item.depot,
        revisionText = item.revision.toString(),
        etaMinutes = item.etaMinutes.toString(),
        budget = "%.2f".format(item.budget),
        minimum = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffTierViews(items: MutableList<TariffTier>): MutableList<TariffTierView> {
    val views = mutableListOf<TariffTierView>()
    for (item in items) {
        views.add(toTariffTierView(item))
    }
    return views
}

fun sumTariffTierView(view: TariffTierView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedRevision.toDouble() + parsedEtaMinutes.toDouble() + parsedBudget
}
