package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global service plan manifest. */
data class GlobalServicePlanManifestView(
    var codeText: String,
    var label: String,
    var amount: String,
    var ownerId: String,
    var region: String,
    var statusLabel: String,
)

fun toGlobalServicePlanManifestView(item: GlobalServicePlanManifest): GlobalServicePlanManifestView {
    return GlobalServicePlanManifestView(
        codeText = truncate(item.code, 40),
        label = item.label,
        amount = "%.2f".format(item.amount),
        ownerId = item.ownerId,
        region = truncate(item.region, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalServicePlanManifestViews(items: MutableList<GlobalServicePlanManifest>): MutableList<GlobalServicePlanManifestView> {
    val views = mutableListOf<GlobalServicePlanManifestView>()
    for (item in items) {
        views.add(toGlobalServicePlanManifestView(item))
    }
    return views
}

fun sumGlobalServicePlanManifestView(view: GlobalServicePlanManifestView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
