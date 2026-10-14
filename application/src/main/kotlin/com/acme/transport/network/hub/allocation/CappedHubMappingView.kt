package com.acme.transport.network.hub.allocation

/** How screens and exports show a capped hub mapping. */
data class CappedHubMappingView(
    var codeText: String,
    var name: String,
    var countryText: String,
    var batchSize: String,
    var discountText: String,
    var ownerId: String,
    var retries: String,
    var version: String,
    var statusLabel: String,
)

fun toCappedHubMappingView(item: CappedHubMapping): CappedHubMappingView {
    return CappedHubMappingView(
        codeText = item.code,
        name = item.name,
        countryText = item.country,
        batchSize = item.batchSize.toString(),
        discountText = "%.2f".format(item.discount),
        ownerId = item.ownerId,
        retries = item.retries.toString(),
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedHubMappingViews(items: MutableList<CappedHubMapping>): MutableList<CappedHubMappingView> {
    val views = mutableListOf<CappedHubMappingView>()
    for (item in items) {
        views.add(toCappedHubMappingView(item))
    }
    return views
}

fun sumCappedHubMappingView(view: CappedHubMappingView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDiscount = view.discountText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedBatchSize.toDouble() + parsedDiscount + parsedRetries.toDouble()
}
