package com.acme.transport.crewing.swap.mapping

/** How screens and exports show a swap mapping. */
data class SwapMappingView(
    var code: String,
    var label: String,
    var ceiling: String,
    var scoreText: String,
    var expiresAt: String,
    var loadKg: String,
    var margin: String,
    var floorValue: String,
    var checksum: String,
    var statusLabel: String,
)

fun toSwapMappingView(item: SwapMapping): SwapMappingView {
    return SwapMappingView(
        code = item.code,
        label = item.label,
        ceiling = "%.2f".format(item.ceiling),
        scoreText = "%.2f".format(item.score),
        expiresAt = item.expiresAt.toString(),
        loadKg = "%.2f".format(item.loadKg),
        margin = "%.2f".format(item.margin),
        floorValue = "%.2f".format(item.floorValue),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapMappingViews(items: MutableList<SwapMapping>): MutableList<SwapMappingView> {
    val views = mutableListOf<SwapMappingView>()
    for (item in items) {
        views.add(toSwapMappingView(item))
    }
    return views
}

fun sumSwapMappingView(view: SwapMappingView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedCeiling + parsedScore + parsedExpiresAt.toDouble()
}
