package com.acme.transport.crewing.swap.claim

/** How screens and exports show a swap manifest. */
data class SwapManifestView(
    var code: String,
    var title: String,
    var country: String,
    var trailerId: String,
    var active: String,
    var routeCode: String,
    var score: String,
    var statusLabel: String,
)

fun toSwapManifestView(item: SwapManifest): SwapManifestView {
    return SwapManifestView(
        code = item.code,
        title = item.title,
        country = item.country,
        trailerId = item.trailerId,
        active = "${item.active}",
        routeCode = item.routeCode,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSwapManifestViews(items: MutableList<SwapManifest>): MutableList<SwapManifestView> {
    val views = mutableListOf<SwapManifestView>()
    for (item in items) {
        views.add(toSwapManifestView(item))
    }
    return views
}

fun sumSwapManifestView(view: SwapManifestView): Double {
    val parsedScore = view.score.toDouble()
    return parsedScore
}
