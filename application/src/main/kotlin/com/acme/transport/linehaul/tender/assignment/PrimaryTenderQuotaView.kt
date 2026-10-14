package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary tender quota. */
data class PrimaryTenderQuotaView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var externalRefText: String,
    var marginText: String,
    var phone: String,
    var updatedAt: String,
    var laneText: String,
    var retries: String,
    var version: String,
    var labelText: String,
    var statusLabel: String,
)

fun toPrimaryTenderQuotaView(item: PrimaryTenderQuota): PrimaryTenderQuotaView {
    return PrimaryTenderQuotaView(
        codeText = item.code,
        title = truncate(item.title, 40),
        ceiling = "%.2f".format(item.ceiling),
        externalRefText = item.externalRef,
        marginText = "%.2f".format(item.margin),
        phone = item.phone,
        updatedAt = item.updatedAt.toString(),
        laneText = item.lane,
        retries = item.retries.toString(),
        version = item.version.toString(),
        labelText = item.label,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTenderQuotaViews(items: MutableList<PrimaryTenderQuota>): MutableList<PrimaryTenderQuotaView> {
    val views = mutableListOf<PrimaryTenderQuotaView>()
    for (item in items) {
        views.add(toPrimaryTenderQuotaView(item))
    }
    return views
}

fun sumPrimaryTenderQuotaView(view: PrimaryTenderQuotaView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedMargin = view.marginText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedCeiling + parsedMargin + parsedUpdatedAt.toDouble()
}
