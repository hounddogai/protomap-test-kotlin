package com.acme.transport.customs.declaration.audit

/** How screens and exports show a manual declaration batch. */
data class ManualDeclarationBatchView(
    var code: String,
    var name: String,
    var channel: String,
    var minimumText: String,
    var updatedAt: String,
    var category: String,
    var fuelLiters: String,
    var etaMinutes: String,
    var sequence: String,
    var statusLabel: String,
)

fun toManualDeclarationBatchView(item: ManualDeclarationBatch): ManualDeclarationBatchView {
    return ManualDeclarationBatchView(
        code = item.code,
        name = item.name,
        channel = item.channel,
        minimumText = "%.2f".format(item.minimum),
        updatedAt = item.updatedAt.toString(),
        category = item.category,
        fuelLiters = "%.2f".format(item.fuelLiters),
        etaMinutes = item.etaMinutes.toString(),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualDeclarationBatchViews(items: MutableList<ManualDeclarationBatch>): MutableList<ManualDeclarationBatchView> {
    val views = mutableListOf<ManualDeclarationBatchView>()
    for (item in items) {
        views.add(toManualDeclarationBatchView(item))
    }
    return views
}

fun sumManualDeclarationBatchView(view: ManualDeclarationBatchView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedMinimum + parsedUpdatedAt.toDouble() + parsedFuelLiters
}
