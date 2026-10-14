package com.acme.transport.freightpay.fuelsurcharge.entry

/** How screens and exports show a fuel surcharge allocation. */
data class FuelSurchargeAllocationView(
    var codeText: String,
    var titleText: String,
    var notes: String,
    var contactEmail: String,
    var localeText: String,
    var rate: String,
    var source: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toFuelSurchargeAllocationView(item: FuelSurchargeAllocation): FuelSurchargeAllocationView {
    return FuelSurchargeAllocationView(
        codeText = item.code,
        titleText = item.title,
        notes = item.notes,
        contactEmail = item.contactEmail,
        localeText = item.locale,
        rate = "%.2f".format(item.rate),
        source = item.source,
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeAllocationViews(items: MutableList<FuelSurchargeAllocation>): MutableList<FuelSurchargeAllocationView> {
    val views = mutableListOf<FuelSurchargeAllocationView>()
    for (item in items) {
        views.add(toFuelSurchargeAllocationView(item))
    }
    return views
}

fun sumFuelSurchargeAllocationView(view: FuelSurchargeAllocationView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedRate + parsedStartsAt.toDouble()
}
