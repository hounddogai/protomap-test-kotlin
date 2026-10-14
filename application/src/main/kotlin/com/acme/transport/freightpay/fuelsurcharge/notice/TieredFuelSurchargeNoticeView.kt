package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered fuel surcharge notice. */
data class TieredFuelSurchargeNoticeView(
    var code: String,
    var labelText: String,
    var batchSize: String,
    var quantity: String,
    var routeCode: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toTieredFuelSurchargeNoticeView(item: TieredFuelSurchargeNotice): TieredFuelSurchargeNoticeView {
    return TieredFuelSurchargeNoticeView(
        code = truncate(item.code, 120),
        labelText = item.label,
        batchSize = item.batchSize.toString(),
        quantity = item.quantity.toString(),
        routeCode = item.routeCode,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelSurchargeNoticeViews(items: MutableList<TieredFuelSurchargeNotice>): MutableList<TieredFuelSurchargeNoticeView> {
    val views = mutableListOf<TieredFuelSurchargeNoticeView>()
    for (item in items) {
        views.add(toTieredFuelSurchargeNoticeView(item))
    }
    return views
}

fun sumTieredFuelSurchargeNoticeView(view: TieredFuelSurchargeNoticeView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBatchSize.toDouble() + parsedQuantity.toDouble() + parsedLoadKg
}
