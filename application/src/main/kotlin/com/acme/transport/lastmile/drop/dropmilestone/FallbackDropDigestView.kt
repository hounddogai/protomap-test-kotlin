package com.acme.transport.lastmile.drop.dropmilestone

/** How screens and exports show a fallback drop digest. */
data class FallbackDropDigestView(
    var code: String,
    var label: String,
    var revisionText: String,
    var batchSizeText: String,
    var floorValue: String,
    var weight: String,
    var minimum: String,
    var externalRef: String,
    var odometer: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toFallbackDropDigestView(item: FallbackDropDigest): FallbackDropDigestView {
    return FallbackDropDigestView(
        code = item.code,
        label = item.label,
        revisionText = item.revision.toString(),
        batchSizeText = item.batchSize.toString(),
        floorValue = "%.2f".format(item.floorValue),
        weight = "%.2f".format(item.weight),
        minimum = "%.2f".format(item.minimum),
        externalRef = item.externalRef,
        odometer = item.odometer.toString(),
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackDropDigestViews(items: MutableList<FallbackDropDigest>): MutableList<FallbackDropDigestView> {
    val views = mutableListOf<FallbackDropDigestView>()
    for (item in items) {
        views.add(toFallbackDropDigestView(item))
    }
    return views
}

fun sumFallbackDropDigestView(view: FallbackDropDigestView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedRevision.toDouble() + parsedBatchSize.toDouble() + parsedFloorValue
}
