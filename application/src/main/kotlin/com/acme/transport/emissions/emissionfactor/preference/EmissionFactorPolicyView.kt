package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor policy. */
data class EmissionFactorPolicyView(
    var code: String,
    var labelText: String,
    var ownerIdText: String,
    var summary: String,
    var rate: String,
    var checksumText: String,
    var limit: String,
    var threshold: String,
    var statusLabel: String,
)

fun toEmissionFactorPolicyView(item: EmissionFactorPolicy): EmissionFactorPolicyView {
    return EmissionFactorPolicyView(
        code = item.code,
        labelText = item.label,
        ownerIdText = item.ownerId,
        summary = item.summary,
        rate = "%.2f".format(item.rate),
        checksumText = truncate(item.checksum, 64),
        limit = item.limit.toString(),
        threshold = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorPolicyViews(items: MutableList<EmissionFactorPolicy>): MutableList<EmissionFactorPolicyView> {
    val views = mutableListOf<EmissionFactorPolicyView>()
    for (item in items) {
        views.add(toEmissionFactorPolicyView(item))
    }
    return views
}

fun sumEmissionFactorPolicyView(view: EmissionFactorPolicyView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedLimit = view.limit.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRate + parsedLimit.toDouble() + parsedThreshold
}
