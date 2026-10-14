package com.acme.transport.linehaul.capacity.assignment

/** How screens and exports show a regional capacity agreement. */
data class RegionalCapacityAgreementView(
    var code: String,
    var label: String,
    var quantityText: String,
    var fuelLiters: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toRegionalCapacityAgreementView(item: RegionalCapacityAgreement): RegionalCapacityAgreementView {
    return RegionalCapacityAgreementView(
        code = item.code,
        label = item.label,
        quantityText = item.quantity.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        vehicleIdText = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityAgreementViews(items: MutableList<RegionalCapacityAgreement>): MutableList<RegionalCapacityAgreementView> {
    val views = mutableListOf<RegionalCapacityAgreementView>()
    for (item in items) {
        views.add(toRegionalCapacityAgreementView(item))
    }
    return views
}

fun sumRegionalCapacityAgreementView(view: RegionalCapacityAgreementView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedQuantity.toDouble() + parsedFuelLiters
}
