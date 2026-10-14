package com.acme.transport.linehaul.laneplan.exception

/** How screens and exports show a lane plan request. */
data class LanePlanRequestView(
    var codeText: String,
    var labelText: String,
    var pallets: String,
    var rateText: String,
    var limit: String,
    var statusLabel: String,
)

fun toLanePlanRequestView(item: LanePlanRequest): LanePlanRequestView {
    return LanePlanRequestView(
        codeText = item.code,
        labelText = item.label,
        pallets = item.pallets.toString(),
        rateText = "%.2f".format(item.rate),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanRequestViews(items: MutableList<LanePlanRequest>): MutableList<LanePlanRequestView> {
    val views = mutableListOf<LanePlanRequestView>()
    for (item in items) {
        views.add(toLanePlanRequestView(item))
    }
    return views
}

fun sumLanePlanRequestView(view: LanePlanRequestView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedRate = view.rateText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedPallets.toDouble() + parsedRate + parsedLimit.toDouble()
}
