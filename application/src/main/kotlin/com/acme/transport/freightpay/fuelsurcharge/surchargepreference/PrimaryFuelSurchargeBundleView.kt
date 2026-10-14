package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

/** How screens and exports show a primary fuel surcharge bundle. */
data class PrimaryFuelSurchargeBundleView(
    var codeText: String,
    var labelText: String,
    var score: String,
    var visibleText: String,
    var channel: String,
    var batchSizeText: String,
    var statusLabel: String,
)

fun toPrimaryFuelSurchargeBundleView(item: PrimaryFuelSurchargeBundle): PrimaryFuelSurchargeBundleView {
    return PrimaryFuelSurchargeBundleView(
        codeText = item.code,
        labelText = item.label,
        score = "%.2f".format(item.score),
        visibleText = "${item.visible}",
        channel = item.channel,
        batchSizeText = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelSurchargeBundleViews(items: MutableList<PrimaryFuelSurchargeBundle>): MutableList<PrimaryFuelSurchargeBundleView> {
    val views = mutableListOf<PrimaryFuelSurchargeBundleView>()
    for (item in items) {
        views.add(toPrimaryFuelSurchargeBundleView(item))
    }
    return views
}

fun sumPrimaryFuelSurchargeBundleView(view: PrimaryFuelSurchargeBundleView): Double {
    val parsedScore = view.score.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedScore + parsedBatchSize.toDouble()
}
