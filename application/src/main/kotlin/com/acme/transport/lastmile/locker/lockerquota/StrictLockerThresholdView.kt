package com.acme.transport.lastmile.locker.lockerquota

/** How screens and exports show a strict locker threshold. */
data class StrictLockerThresholdView(
    var code: String,
    var name: String,
    var externalRef: String,
    var weight: String,
    var vehicleId: String,
    var country: String,
    var phone: String,
    var statusLabel: String,
)

fun toStrictLockerThresholdView(item: StrictLockerThreshold): StrictLockerThresholdView {
    return StrictLockerThresholdView(
        code = item.code,
        name = item.name,
        externalRef = item.externalRef,
        weight = "%.2f".format(item.weight),
        vehicleId = item.vehicleId,
        country = item.country,
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictLockerThresholdViews(items: MutableList<StrictLockerThreshold>): MutableList<StrictLockerThresholdView> {
    val views = mutableListOf<StrictLockerThresholdView>()
    for (item in items) {
        views.add(toStrictLockerThresholdView(item))
    }
    return views
}

fun sumStrictLockerThresholdView(view: StrictLockerThresholdView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
