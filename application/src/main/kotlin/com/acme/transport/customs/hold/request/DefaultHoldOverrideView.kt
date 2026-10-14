package com.acme.transport.customs.hold.request

/** How screens and exports show a default hold override. */
data class DefaultHoldOverrideView(
    var code: String,
    var label: String,
    var quantityText: String,
    var regionText: String,
    var nameText: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toDefaultHoldOverrideView(item: DefaultHoldOverride): DefaultHoldOverrideView {
    return DefaultHoldOverrideView(
        code = item.code,
        label = item.label,
        quantityText = item.quantity.toString(),
        regionText = item.region,
        nameText = item.name,
        descriptionText = item.description,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultHoldOverrideViews(items: MutableList<DefaultHoldOverride>): MutableList<DefaultHoldOverrideView> {
    val views = mutableListOf<DefaultHoldOverrideView>()
    for (item in items) {
        views.add(toDefaultHoldOverrideView(item))
    }
    return views
}

fun sumDefaultHoldOverrideView(view: DefaultHoldOverrideView): Double {
    val parsedQuantity = view.quantityText.toInt()
    return parsedQuantity.toDouble()
}
