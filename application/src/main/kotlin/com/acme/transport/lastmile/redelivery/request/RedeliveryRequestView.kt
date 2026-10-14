package com.acme.transport.lastmile.redelivery.request

/** How screens and exports show a redelivery request. */
data class RedeliveryRequestView(
    var code: String,
    var name: String,
    var retries: String,
    var category: String,
    var description: String,
    var localeText: String,
    var titleText: String,
    var version: String,
    var rate: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toRedeliveryRequestView(item: RedeliveryRequest): RedeliveryRequestView {
    return RedeliveryRequestView(
        code = item.code,
        name = item.name,
        retries = item.retries.toString(),
        category = item.category,
        description = item.description,
        localeText = item.locale,
        titleText = item.title,
        version = item.version.toString(),
        rate = "%.2f".format(item.rate),
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryRequestViews(items: MutableList<RedeliveryRequest>): MutableList<RedeliveryRequestView> {
    val views = mutableListOf<RedeliveryRequestView>()
    for (item in items) {
        views.add(toRedeliveryRequestView(item))
    }
    return views
}

fun sumRedeliveryRequestView(view: RedeliveryRequestView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedVersion = view.version.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedRetries.toDouble() + parsedVersion.toDouble() + parsedRate
}
