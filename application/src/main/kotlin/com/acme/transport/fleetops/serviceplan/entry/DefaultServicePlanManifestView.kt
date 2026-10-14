package com.acme.transport.fleetops.serviceplan.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default service plan manifest. */
data class DefaultServicePlanManifestView(
    var code: String,
    var title: String,
    var archived: String,
    var batchSizeText: String,
    var ratioText: String,
    var weight: String,
    var revision: String,
    var amountText: String,
    var visible: String,
    var label: String,
    var segment: String,
    var statusLabel: String,
)

fun toDefaultServicePlanManifestView(item: DefaultServicePlanManifest): DefaultServicePlanManifestView {
    return DefaultServicePlanManifestView(
        code = item.code,
        title = truncate(item.title, 120),
        archived = "${item.archived}",
        batchSizeText = item.batchSize.toString(),
        ratioText = "%.2f".format(item.ratio),
        weight = "%.2f".format(item.weight),
        revision = item.revision.toString(),
        amountText = "%.2f".format(item.amount),
        visible = "${item.visible}",
        label = item.label,
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultServicePlanManifestViews(items: MutableList<DefaultServicePlanManifest>): MutableList<DefaultServicePlanManifestView> {
    val views = mutableListOf<DefaultServicePlanManifestView>()
    for (item in items) {
        views.add(toDefaultServicePlanManifestView(item))
    }
    return views
}

fun sumDefaultServicePlanManifestView(view: DefaultServicePlanManifestView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRatio = view.ratioText.toDouble()
    val parsedWeight = view.weight.toDouble()
    return parsedBatchSize.toDouble() + parsedRatio + parsedWeight
}
