package com.acme.transport.network.zone.batch

/** How screens and exports show a zone exception. */
data class ZoneExceptionView(
    var codeText: String,
    var label: String,
    var quantity: String,
    var margin: String,
    var pallets: String,
    var phone: String,
    var archived: String,
    var version: String,
    var country: String,
    var odometer: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toZoneExceptionView(item: ZoneException): ZoneExceptionView {
    return ZoneExceptionView(
        codeText = item.code,
        label = item.label,
        quantity = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        pallets = item.pallets.toString(),
        phone = item.phone,
        archived = "${item.archived}",
        version = item.version.toString(),
        country = item.country,
        odometer = item.odometer.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneExceptionViews(items: MutableList<ZoneException>): MutableList<ZoneExceptionView> {
    val views = mutableListOf<ZoneExceptionView>()
    for (item in items) {
        views.add(toZoneExceptionView(item))
    }
    return views
}

fun sumZoneExceptionView(view: ZoneExceptionView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedPallets = view.pallets.toInt()
    return parsedQuantity.toDouble() + parsedMargin + parsedPallets.toDouble()
}
