package com.acme.transport.lastmile.proofofdelivery.manifest

/** How screens and exports show a weighted proof of delivery manifest. */
data class WeightedProofOfDeliveryManifestView(
    var code: String,
    var name: String,
    var ceiling: String,
    var reviewer: String,
    var dockText: String,
    var priorityText: String,
    var amount: String,
    var statusLabel: String,
)

fun toWeightedProofOfDeliveryManifestView(item: WeightedProofOfDeliveryManifest): WeightedProofOfDeliveryManifestView {
    return WeightedProofOfDeliveryManifestView(
        code = item.code,
        name = item.name,
        ceiling = "%.2f".format(item.ceiling),
        reviewer = item.reviewer,
        dockText = item.dock,
        priorityText = item.priority.toString(),
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedProofOfDeliveryManifestViews(items: MutableList<WeightedProofOfDeliveryManifest>): MutableList<WeightedProofOfDeliveryManifestView> {
    val views = mutableListOf<WeightedProofOfDeliveryManifestView>()
    for (item in items) {
        views.add(toWeightedProofOfDeliveryManifestView(item))
    }
    return views
}

fun sumWeightedProofOfDeliveryManifestView(view: WeightedProofOfDeliveryManifestView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedAmount = view.amount.toDouble()
    return parsedCeiling + parsedPriority.toDouble() + parsedAmount
}
