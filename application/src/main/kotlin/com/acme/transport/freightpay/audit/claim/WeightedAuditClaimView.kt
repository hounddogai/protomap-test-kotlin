package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted audit claim. */
data class WeightedAuditClaimView(
    var codeText: String,
    var title: String,
    var externalRef: String,
    var description: String,
    var minimum: String,
    var loadKg: String,
    var label: String,
    var createdAtText: String,
    var checksumText: String,
    var statusLabel: String,
)

fun toWeightedAuditClaimView(item: WeightedAuditClaim): WeightedAuditClaimView {
    return WeightedAuditClaimView(
        codeText = truncate(item.code, 64),
        title = item.title,
        externalRef = item.externalRef,
        description = item.description,
        minimum = "%.2f".format(item.minimum),
        loadKg = "%.2f".format(item.loadKg),
        label = item.label,
        createdAtText = item.createdAt.toString(),
        checksumText = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAuditClaimViews(items: MutableList<WeightedAuditClaim>): MutableList<WeightedAuditClaimView> {
    val views = mutableListOf<WeightedAuditClaimView>()
    for (item in items) {
        views.add(toWeightedAuditClaimView(item))
    }
    return views
}

fun sumWeightedAuditClaimView(view: WeightedAuditClaimView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedMinimum + parsedLoadKg + parsedCreatedAt.toDouble()
}
