package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback emission factor policy. */
data class FallbackEmissionFactorPolicyView(
    var code: String,
    var label: String,
    var quantity: String,
    var createdAt: String,
    var versionText: String,
    var regionText: String,
    var summary: String,
    var pinnedText: String,
    var amount: String,
    var statusLabel: String,
)

fun toFallbackEmissionFactorPolicyView(item: FallbackEmissionFactorPolicy): FallbackEmissionFactorPolicyView {
    return FallbackEmissionFactorPolicyView(
        code = item.code,
        label = item.label,
        quantity = item.quantity.toString(),
        createdAt = item.createdAt.toString(),
        versionText = item.version.toString(),
        regionText = item.region,
        summary = truncate(item.summary, 120),
        pinnedText = "${item.pinned}",
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackEmissionFactorPolicyViews(items: MutableList<FallbackEmissionFactorPolicy>): MutableList<FallbackEmissionFactorPolicyView> {
    val views = mutableListOf<FallbackEmissionFactorPolicyView>()
    for (item in items) {
        views.add(toFallbackEmissionFactorPolicyView(item))
    }
    return views
}

fun sumFallbackEmissionFactorPolicyView(view: FallbackEmissionFactorPolicyView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedVersion = view.versionText.toInt()
    return parsedQuantity.toDouble() + parsedCreatedAt.toDouble() + parsedVersion.toDouble()
}
