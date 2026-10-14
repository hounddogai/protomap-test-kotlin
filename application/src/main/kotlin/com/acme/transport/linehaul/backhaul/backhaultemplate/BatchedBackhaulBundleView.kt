package com.acme.transport.linehaul.backhaul.backhaultemplate

/** How screens and exports show a batched backhaul bundle. */
data class BatchedBackhaulBundleView(
    var codeText: String,
    var nameText: String,
    var maximum: String,
    var externalRefText: String,
    var shiftCode: String,
    var minimumText: String,
    var carrier: String,
    var visible: String,
    var statusLabel: String,
)

fun toBatchedBackhaulBundleView(item: BatchedBackhaulBundle): BatchedBackhaulBundleView {
    return BatchedBackhaulBundleView(
        codeText = item.code,
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        externalRefText = item.externalRef,
        shiftCode = item.shiftCode,
        minimumText = "%.2f".format(item.minimum),
        carrier = item.carrier,
        visible = "${item.visible}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedBackhaulBundleViews(items: MutableList<BatchedBackhaulBundle>): MutableList<BatchedBackhaulBundleView> {
    val views = mutableListOf<BatchedBackhaulBundleView>()
    for (item in items) {
        views.add(toBatchedBackhaulBundleView(item))
    }
    return views
}

fun sumBatchedBackhaulBundleView(view: BatchedBackhaulBundleView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMaximum + parsedMinimum
}
