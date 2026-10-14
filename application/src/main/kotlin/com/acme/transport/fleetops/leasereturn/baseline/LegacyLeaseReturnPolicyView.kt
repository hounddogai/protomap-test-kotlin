package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy lease return policy. */
data class LegacyLeaseReturnPolicyView(
    var code: String,
    var titleText: String,
    var country: String,
    var maximumText: String,
    var description: String,
    var statusLabel: String,
)

fun toLegacyLeaseReturnPolicyView(item: LegacyLeaseReturnPolicy): LegacyLeaseReturnPolicyView {
    return LegacyLeaseReturnPolicyView(
        code = item.code,
        titleText = item.title,
        country = item.country,
        maximumText = "%.2f".format(item.maximum),
        description = truncate(item.description, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLeaseReturnPolicyViews(items: MutableList<LegacyLeaseReturnPolicy>): MutableList<LegacyLeaseReturnPolicyView> {
    val views = mutableListOf<LegacyLeaseReturnPolicyView>()
    for (item in items) {
        views.add(toLegacyLeaseReturnPolicyView(item))
    }
    return views
}

fun sumLegacyLeaseReturnPolicyView(view: LegacyLeaseReturnPolicyView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
