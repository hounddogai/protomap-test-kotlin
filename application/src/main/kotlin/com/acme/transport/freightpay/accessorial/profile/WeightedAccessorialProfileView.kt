package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted accessorial profile. */
data class WeightedAccessorialProfileView(
    var code: String,
    var nameText: String,
    var vehicleId: String,
    var summaryText: String,
    var retries: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toWeightedAccessorialProfileView(item: WeightedAccessorialProfile): WeightedAccessorialProfileView {
    return WeightedAccessorialProfileView(
        code = truncate(item.code, 120),
        nameText = item.name,
        vehicleId = item.vehicleId,
        summaryText = item.summary,
        retries = item.retries.toString(),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedAccessorialProfileViews(items: MutableList<WeightedAccessorialProfile>): MutableList<WeightedAccessorialProfileView> {
    val views = mutableListOf<WeightedAccessorialProfileView>()
    for (item in items) {
        views.add(toWeightedAccessorialProfileView(item))
    }
    return views
}

fun sumWeightedAccessorialProfileView(view: WeightedAccessorialProfileView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
