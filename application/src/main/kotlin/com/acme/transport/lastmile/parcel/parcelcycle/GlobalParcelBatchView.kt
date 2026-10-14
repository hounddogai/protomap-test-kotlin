package com.acme.transport.lastmile.parcel.parcelcycle

/** How screens and exports show a global parcel batch. */
data class GlobalParcelBatchView(
    var code: String,
    var name: String,
    var loadKgText: String,
    var retries: String,
    var countryText: String,
    var fuelLitersText: String,
    var category: String,
    var statusLabel: String,
)

fun toGlobalParcelBatchView(item: GlobalParcelBatch): GlobalParcelBatchView {
    return GlobalParcelBatchView(
        code = item.code,
        name = item.name,
        loadKgText = "%.2f".format(item.loadKg),
        retries = item.retries.toString(),
        countryText = item.country,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        category = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalParcelBatchViews(items: MutableList<GlobalParcelBatch>): MutableList<GlobalParcelBatchView> {
    val views = mutableListOf<GlobalParcelBatchView>()
    for (item in items) {
        views.add(toGlobalParcelBatchView(item))
    }
    return views
}

fun sumGlobalParcelBatchView(view: GlobalParcelBatchView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    return parsedLoadKg + parsedRetries.toDouble() + parsedFuelLiters
}
