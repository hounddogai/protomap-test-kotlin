package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global audit batch. */
data class GlobalAuditBatchView(
    var code: String,
    var label: String,
    var reviewer: String,
    var country: String,
    var currency: String,
    var capacityText: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalAuditBatchView(item: GlobalAuditBatch): GlobalAuditBatchView {
    return GlobalAuditBatchView(
        code = truncate(item.code, 64),
        label = item.label,
        reviewer = truncate(item.reviewer, 120),
        country = item.country,
        currency = item.currency,
        capacityText = item.capacity.toString(),
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalAuditBatchViews(items: MutableList<GlobalAuditBatch>): MutableList<GlobalAuditBatchView> {
    val views = mutableListOf<GlobalAuditBatchView>()
    for (item in items) {
        views.add(toGlobalAuditBatchView(item))
    }
    return views
}

fun sumGlobalAuditBatchView(view: GlobalAuditBatchView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
