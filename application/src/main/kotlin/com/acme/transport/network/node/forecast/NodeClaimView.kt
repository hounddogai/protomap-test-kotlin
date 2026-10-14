package com.acme.transport.network.node.forecast

/** How screens and exports show a node claim. */
data class NodeClaimView(
    var code: String,
    var name: String,
    var revision: String,
    var loadKg: String,
    var notes: String,
    var rateText: String,
    var statusLabel: String,
)

fun toNodeClaimView(item: NodeClaim): NodeClaimView {
    return NodeClaimView(
        code = item.code,
        name = item.name,
        revision = item.revision.toString(),
        loadKg = "%.2f".format(item.loadKg),
        notes = item.notes,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeClaimViews(items: MutableList<NodeClaim>): MutableList<NodeClaimView> {
    val views = mutableListOf<NodeClaimView>()
    for (item in items) {
        views.add(toNodeClaimView(item))
    }
    return views
}

fun sumNodeClaimView(view: NodeClaimView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedRevision.toDouble() + parsedLoadKg + parsedRate
}
