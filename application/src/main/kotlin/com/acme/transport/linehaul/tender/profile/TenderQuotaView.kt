package com.acme.transport.linehaul.tender.profile

/** How screens and exports show a tender quota. */
data class TenderQuotaView(
    var codeText: String,
    var label: String,
    var phoneText: String,
    var reviewer: String,
    var ownerId: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toTenderQuotaView(item: TenderQuota): TenderQuotaView {
    return TenderQuotaView(
        codeText = item.code,
        label = item.label,
        phoneText = item.phone,
        reviewer = item.reviewer,
        ownerId = item.ownerId,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTenderQuotaViews(items: MutableList<TenderQuota>): MutableList<TenderQuotaView> {
    val views = mutableListOf<TenderQuotaView>()
    for (item in items) {
        views.add(toTenderQuotaView(item))
    }
    return views
}

fun sumTenderQuotaView(view: TenderQuotaView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
