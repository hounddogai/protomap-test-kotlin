package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration batch. */
data class DeclarationBatchView(
    var code: String,
    var titleText: String,
    var pallets: String,
    var stopCount: String,
    var archived: String,
    var statusLabel: String,
)

fun toDeclarationBatchView(item: DeclarationBatch): DeclarationBatchView {
    return DeclarationBatchView(
        code = truncate(item.code, 64),
        titleText = truncate(item.title, 120),
        pallets = item.pallets.toString(),
        stopCount = item.stopCount.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationBatchViews(items: MutableList<DeclarationBatch>): MutableList<DeclarationBatchView> {
    val views = mutableListOf<DeclarationBatchView>()
    for (item in items) {
        views.add(toDeclarationBatchView(item))
    }
    return views
}

fun sumDeclarationBatchView(view: DeclarationBatchView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPallets.toDouble() + parsedStopCount.toDouble()
}
