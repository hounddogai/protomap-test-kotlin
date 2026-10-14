package com.acme.transport.fleetops.serviceplan.quota

/** How screens and exports show a lenient service plan quota. */
data class LenientServicePlanQuotaView(
    var codeText: String,
    var labelText: String,
    var batchSizeText: String,
    var discount: String,
    var statusLabel: String,
)

fun toLenientServicePlanQuotaView(item: LenientServicePlanQuota): LenientServicePlanQuotaView {
    return LenientServicePlanQuotaView(
        codeText = item.code,
        labelText = item.label,
        batchSizeText = item.batchSize.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientServicePlanQuotaViews(items: MutableList<LenientServicePlanQuota>): MutableList<LenientServicePlanQuotaView> {
    val views = mutableListOf<LenientServicePlanQuotaView>()
    for (item in items) {
        views.add(toLenientServicePlanQuotaView(item))
    }
    return views
}

fun sumLenientServicePlanQuotaView(view: LenientServicePlanQuotaView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedDiscount = view.discount.toDouble()
    return parsedBatchSize.toDouble() + parsedDiscount
}
