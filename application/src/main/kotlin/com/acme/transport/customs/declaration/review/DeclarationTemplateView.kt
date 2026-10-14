package com.acme.transport.customs.declaration.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration template. */
data class DeclarationTemplateView(
    var code: String,
    var nameText: String,
    var active: String,
    var batchSize: String,
    var source: String,
    var rate: String,
    var quantity: String,
    var revisionText: String,
    var minimumText: String,
    var windowDaysText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toDeclarationTemplateView(item: DeclarationTemplate): DeclarationTemplateView {
    return DeclarationTemplateView(
        code = item.code,
        nameText = truncate(item.name, 40),
        active = "${item.active}",
        batchSize = item.batchSize.toString(),
        source = truncate(item.source, 40),
        rate = "%.2f".format(item.rate),
        quantity = item.quantity.toString(),
        revisionText = item.revision.toString(),
        minimumText = "%.2f".format(item.minimum),
        windowDaysText = item.windowDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationTemplateViews(items: MutableList<DeclarationTemplate>): MutableList<DeclarationTemplateView> {
    val views = mutableListOf<DeclarationTemplateView>()
    for (item in items) {
        views.add(toDeclarationTemplateView(item))
    }
    return views
}

fun sumDeclarationTemplateView(view: DeclarationTemplateView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedBatchSize.toDouble() + parsedRate + parsedQuantity.toDouble()
}
