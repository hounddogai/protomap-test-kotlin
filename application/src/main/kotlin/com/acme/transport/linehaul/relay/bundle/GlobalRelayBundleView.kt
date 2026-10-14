package com.acme.transport.linehaul.relay.bundle

/** How screens and exports show a global relay bundle. */
data class GlobalRelayBundleView(
    var code: String,
    var labelText: String,
    var minimumText: String,
    var graceDays: String,
    var expiresAtText: String,
    var visibleText: String,
    var marginText: String,
    var description: String,
    var pallets: String,
    var statusLabel: String,
)

fun toGlobalRelayBundleView(item: GlobalRelayBundle): GlobalRelayBundleView {
    return GlobalRelayBundleView(
        code = item.code,
        labelText = item.label,
        minimumText = "%.2f".format(item.minimum),
        graceDays = item.graceDays.toString(),
        expiresAtText = item.expiresAt.toString(),
        visibleText = "${item.visible}",
        marginText = "%.2f".format(item.margin),
        description = item.description,
        pallets = item.pallets.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalRelayBundleViews(items: MutableList<GlobalRelayBundle>): MutableList<GlobalRelayBundleView> {
    val views = mutableListOf<GlobalRelayBundleView>()
    for (item in items) {
        views.add(toGlobalRelayBundleView(item))
    }
    return views
}

fun sumGlobalRelayBundleView(view: GlobalRelayBundleView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedMinimum + parsedGraceDays.toDouble() + parsedExpiresAt.toDouble()
}
