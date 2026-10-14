package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted tariff audit. */
data class WeightedTariffAuditView(
    var code: String,
    var label: String,
    var rate: String,
    var timezone: String,
    var ceiling: String,
    var approved: String,
    var revision: String,
    var statusLabel: String,
)

fun toWeightedTariffAuditView(item: WeightedTariffAudit): WeightedTariffAuditView {
    return WeightedTariffAuditView(
        code = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        timezone = truncate(item.timezone, 120),
        ceiling = "%.2f".format(item.ceiling),
        approved = "${item.approved}",
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTariffAuditViews(items: MutableList<WeightedTariffAudit>): MutableList<WeightedTariffAuditView> {
    val views = mutableListOf<WeightedTariffAuditView>()
    for (item in items) {
        views.add(toWeightedTariffAuditView(item))
    }
    return views
}

fun sumWeightedTariffAuditView(view: WeightedTariffAuditView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedRate + parsedCeiling + parsedRevision.toDouble()
}
