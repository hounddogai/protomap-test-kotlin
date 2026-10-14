package com.acme.transport.lastmile.parcel.parcelslot

/** How screens and exports show a batched parcel forecast. */
data class BatchedParcelForecastView(
    var codeText: String,
    var title: String,
    var checksumText: String,
    var carrier: String,
    var contactEmail: String,
    var vehicleId: String,
    var description: String,
    var amount: String,
    var statusLabel: String,
)

fun toBatchedParcelForecastView(item: BatchedParcelForecast): BatchedParcelForecastView {
    return BatchedParcelForecastView(
        codeText = item.code,
        title = item.title,
        checksumText = item.checksum,
        carrier = item.carrier,
        contactEmail = item.contactEmail,
        vehicleId = item.vehicleId,
        description = item.description,
        amount = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedParcelForecastViews(items: MutableList<BatchedParcelForecast>): MutableList<BatchedParcelForecastView> {
    val views = mutableListOf<BatchedParcelForecastView>()
    for (item in items) {
        views.add(toBatchedParcelForecastView(item))
    }
    return views
}

fun sumBatchedParcelForecastView(view: BatchedParcelForecastView): Double {
    val parsedAmount = view.amount.toDouble()
    return parsedAmount
}
