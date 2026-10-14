package com.acme.transport.fleetops.serviceplan.legacyplanquota

/** How screens and exports show a capped service plan quota. */
data class CappedServicePlanQuotaView(
    var code: String,
    var titleText: String,
    var source: String,
    var etaMinutes: String,
    var endsAt: String,
    var notes: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toCappedServicePlanQuotaView(item: CappedServicePlanQuota): CappedServicePlanQuotaView {
    return CappedServicePlanQuotaView(
        code = item.code,
        titleText = item.title,
        source = item.source,
        etaMinutes = item.etaMinutes.toString(),
        endsAt = item.endsAt.toString(),
        notes = item.notes,
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServicePlanQuotaViews(items: MutableList<CappedServicePlanQuota>): MutableList<CappedServicePlanQuotaView> {
    val views = mutableListOf<CappedServicePlanQuotaView>()
    for (item in items) {
        views.add(toCappedServicePlanQuotaView(item))
    }
    return views
}

fun sumCappedServicePlanQuotaView(view: CappedServicePlanQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedEtaMinutes.toDouble() + parsedEndsAt.toDouble() + parsedBatchSize.toDouble()
}
