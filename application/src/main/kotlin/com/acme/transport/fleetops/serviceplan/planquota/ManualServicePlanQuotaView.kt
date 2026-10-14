package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual service plan quota. */
data class ManualServicePlanQuotaView(
    var code: String,
    var label: String,
    var checksum: String,
    var expiresAt: String,
    var region: String,
    var title: String,
    var threshold: String,
    var statusLabel: String,
)

fun toManualServicePlanQuotaView(item: ManualServicePlanQuota): ManualServicePlanQuotaView {
    return ManualServicePlanQuotaView(
        code = item.code,
        label = item.label,
        checksum = truncate(item.checksum, 40),
        expiresAt = item.expiresAt.toString(),
        region = item.region,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualServicePlanQuotaViews(items: MutableList<ManualServicePlanQuota>): MutableList<ManualServicePlanQuotaView> {
    val views = mutableListOf<ManualServicePlanQuotaView>()
    for (item in items) {
        views.add(toManualServicePlanQuotaView(item))
    }
    return views
}

fun sumManualServicePlanQuotaView(view: ManualServicePlanQuotaView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedExpiresAt.toDouble() + parsedThreshold
}
