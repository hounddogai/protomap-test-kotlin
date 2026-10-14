package com.acme.transport.network.node.draft

/** How screens and exports show a node score. */
data class NodeScoreView(
    var code: String,
    var nameText: String,
    var retries: String,
    var stopCount: String,
    var score: String,
    var discount: String,
    var versionText: String,
    var category: String,
    var sourceText: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toNodeScoreView(item: NodeScore): NodeScoreView {
    return NodeScoreView(
        code = item.code,
        nameText = item.name,
        retries = item.retries.toString(),
        stopCount = item.stopCount.toString(),
        score = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        versionText = item.version.toString(),
        category = item.category,
        sourceText = item.source,
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeScoreViews(items: MutableList<NodeScore>): MutableList<NodeScoreView> {
    val views = mutableListOf<NodeScoreView>()
    for (item in items) {
        views.add(toNodeScoreView(item))
    }
    return views
}

fun sumNodeScoreView(view: NodeScoreView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedScore = view.score.toDouble()
    return parsedRetries.toDouble() + parsedStopCount.toDouble() + parsedScore
}
