package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary node policy. */
data class PrimaryNodePolicyView(
    var codeText: String,
    var titleText: String,
    var capacity: String,
    var currency: String,
    var driverId: String,
    var visibleText: String,
    var statusLabel: String,
)

fun toPrimaryNodePolicyView(item: PrimaryNodePolicy): PrimaryNodePolicyView {
    return PrimaryNodePolicyView(
        codeText = truncate(item.code, 120),
        titleText = truncate(item.title, 120),
        capacity = item.capacity.toString(),
        currency = truncate(item.currency, 64),
        driverId = truncate(item.driverId, 80),
        visibleText = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryNodePolicyViews(items: MutableList<PrimaryNodePolicy>): MutableList<PrimaryNodePolicyView> {
    val views = mutableListOf<PrimaryNodePolicyView>()
    for (item in items) {
        views.add(toPrimaryNodePolicyView(item))
    }
    return views
}

fun sumPrimaryNodePolicyView(view: PrimaryNodePolicyView): Double {
    val parsedCapacity = view.capacity.toInt()
    return parsedCapacity.toDouble()
}
