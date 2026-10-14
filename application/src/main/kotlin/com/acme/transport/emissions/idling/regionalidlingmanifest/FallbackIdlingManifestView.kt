package com.acme.transport.emissions.idling.regionalidlingmanifest

/** How screens and exports show a fallback idling manifest. */
data class FallbackIdlingManifestView(
    var code: String,
    var titleText: String,
    var fuelLiters: String,
    var contactEmailText: String,
    var quantityText: String,
    var nameText: String,
    var priority: String,
    var floorValueText: String,
    var country: String,
    var endsAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toFallbackIdlingManifestView(item: FallbackIdlingManifest): FallbackIdlingManifestView {
    return FallbackIdlingManifestView(
        code = item.code,
        titleText = item.title,
        fuelLiters = "%.2f".format(item.fuelLiters),
        contactEmailText = item.contactEmail,
        quantityText = item.quantity.toString(),
        nameText = item.name,
        priority = item.priority.toString(),
        floorValueText = "%.2f".format(item.floorValue),
        country = item.country,
        endsAt = item.endsAt.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackIdlingManifestViews(items: MutableList<FallbackIdlingManifest>): MutableList<FallbackIdlingManifestView> {
    val views = mutableListOf<FallbackIdlingManifestView>()
    for (item in items) {
        views.add(toFallbackIdlingManifestView(item))
    }
    return views
}

fun sumFallbackIdlingManifestView(view: FallbackIdlingManifestView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedQuantity = view.quantityText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedFuelLiters + parsedQuantity.toDouble() + parsedPriority.toDouble()
}
