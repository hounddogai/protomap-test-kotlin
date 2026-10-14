package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global qualification bundle. */
data class GlobalQualificationBundleView(
    var code: String,
    var name: String,
    var externalRef: String,
    var titleText: String,
    var summary: String,
    var batchSize: String,
    var archivedText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toGlobalQualificationBundleView(item: GlobalQualificationBundle): GlobalQualificationBundleView {
    return GlobalQualificationBundleView(
        code = item.code,
        name = item.name,
        externalRef = truncate(item.externalRef, 80),
        titleText = truncate(item.title, 80),
        summary = item.summary,
        batchSize = item.batchSize.toString(),
        archivedText = "${item.archived}",
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalQualificationBundleViews(items: MutableList<GlobalQualificationBundle>): MutableList<GlobalQualificationBundleView> {
    val views = mutableListOf<GlobalQualificationBundleView>()
    for (item in items) {
        views.add(toGlobalQualificationBundleView(item))
    }
    return views
}

fun sumGlobalQualificationBundleView(view: GlobalQualificationBundleView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedBatchSize.toDouble() + parsedFloorValue
}
