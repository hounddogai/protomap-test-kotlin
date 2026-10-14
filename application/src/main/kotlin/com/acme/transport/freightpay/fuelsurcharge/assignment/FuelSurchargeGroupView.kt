package com.acme.transport.freightpay.fuelsurcharge.assignment

/** How screens and exports show a fuel surcharge group. */
data class FuelSurchargeGroupView(
    var code: String,
    var title: String,
    var palletsText: String,
    var ownerIdText: String,
    var contactEmailText: String,
    var country: String,
    var score: String,
    var statusLabel: String,
)

fun toFuelSurchargeGroupView(item: FuelSurchargeGroup): FuelSurchargeGroupView {
    return FuelSurchargeGroupView(
        code = item.code,
        title = item.title,
        palletsText = item.pallets.toString(),
        ownerIdText = item.ownerId,
        contactEmailText = item.contactEmail,
        country = item.country,
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelSurchargeGroupViews(items: MutableList<FuelSurchargeGroup>): MutableList<FuelSurchargeGroupView> {
    val views = mutableListOf<FuelSurchargeGroupView>()
    for (item in items) {
        views.add(toFuelSurchargeGroupView(item))
    }
    return views
}

fun sumFuelSurchargeGroupView(view: FuelSurchargeGroupView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedScore = view.score.toDouble()
    return parsedPallets.toDouble() + parsedScore
}
