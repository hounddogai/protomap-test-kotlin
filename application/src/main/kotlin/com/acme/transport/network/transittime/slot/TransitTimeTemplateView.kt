package com.acme.transport.network.transittime.slot

/** How screens and exports show a transit time template. */
data class TransitTimeTemplateView(
    var codeText: String,
    var name: String,
    var description: String,
    var floorValueText: String,
    var shiftCode: String,
    var region: String,
    var statusLabel: String,
)

fun toTransitTimeTemplateView(item: TransitTimeTemplate): TransitTimeTemplateView {
    return TransitTimeTemplateView(
        codeText = item.code,
        name = item.name,
        description = item.description,
        floorValueText = "%.2f".format(item.floorValue),
        shiftCode = item.shiftCode,
        region = item.region,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTransitTimeTemplateViews(items: MutableList<TransitTimeTemplate>): MutableList<TransitTimeTemplateView> {
    val views = mutableListOf<TransitTimeTemplateView>()
    for (item in items) {
        views.add(toTransitTimeTemplateView(item))
    }
    return views
}

fun sumTransitTimeTemplateView(view: TransitTimeTemplateView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedFloorValue
}
