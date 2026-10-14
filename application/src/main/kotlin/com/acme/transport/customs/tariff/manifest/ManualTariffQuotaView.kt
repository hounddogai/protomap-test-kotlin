package com.acme.transport.customs.tariff.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tariff quota. */
data class ManualTariffQuotaView(
    var code: String,
    var title: String,
    var locale: String,
    var reviewerText: String,
    var batchSize: String,
    var segmentText: String,
    var visible: String,
    var active: String,
    var marginText: String,
    var statusLabel: String,
)

fun toManualTariffQuotaView(item: ManualTariffQuota): ManualTariffQuotaView {
    return ManualTariffQuotaView(
        code = truncate(item.code, 64),
        title = item.title,
        locale = item.locale,
        reviewerText = item.reviewer,
        batchSize = item.batchSize.toString(),
        segmentText = truncate(item.segment, 40),
        visible = "${item.visible}",
        active = "${item.active}",
        marginText = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTariffQuotaViews(items: MutableList<ManualTariffQuota>): MutableList<ManualTariffQuotaView> {
    val views = mutableListOf<ManualTariffQuotaView>()
    for (item in items) {
        views.add(toManualTariffQuotaView(item))
    }
    return views
}

fun sumManualTariffQuotaView(view: ManualTariffQuotaView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedMargin = view.marginText.toDouble()
    return parsedBatchSize.toDouble() + parsedMargin
}
