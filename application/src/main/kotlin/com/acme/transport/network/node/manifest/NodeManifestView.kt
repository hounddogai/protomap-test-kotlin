package com.acme.transport.network.node.manifest

/** How screens and exports show a node manifest. */
data class NodeManifestView(
    var code: String,
    var title: String,
    var ownerId: String,
    var limit: String,
    var loadKgText: String,
    var visibleText: String,
    var endsAtText: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toNodeManifestView(item: NodeManifest): NodeManifestView {
    return NodeManifestView(
        code = item.code,
        title = item.title,
        ownerId = item.ownerId,
        limit = item.limit.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        visibleText = "${item.visible}",
        endsAtText = item.endsAt.toString(),
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeManifestViews(items: MutableList<NodeManifest>): MutableList<NodeManifestView> {
    val views = mutableListOf<NodeManifestView>()
    for (item in items) {
        views.add(toNodeManifestView(item))
    }
    return views
}

fun sumNodeManifestView(view: NodeManifestView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedLimit.toDouble() + parsedLoadKg + parsedEndsAt.toDouble()
}
