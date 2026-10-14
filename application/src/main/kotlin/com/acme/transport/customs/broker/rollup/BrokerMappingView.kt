package com.acme.transport.customs.broker.rollup

/** How screens and exports show a broker mapping. */
data class BrokerMappingView(
    var codeText: String,
    var title: String,
    var score: String,
    var etaMinutes: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toBrokerMappingView(item: BrokerMapping): BrokerMappingView {
    return BrokerMappingView(
        codeText = item.code,
        title = item.title,
        score = "%.2f".format(item.score),
        etaMinutes = item.etaMinutes.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerMappingViews(items: MutableList<BrokerMapping>): MutableList<BrokerMappingView> {
    val views = mutableListOf<BrokerMappingView>()
    for (item in items) {
        views.add(toBrokerMappingView(item))
    }
    return views
}

fun sumBrokerMappingView(view: BrokerMappingView): Double {
    val parsedScore = view.score.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedScore + parsedEtaMinutes.toDouble() + parsedDurationMinutes.toDouble()
}
