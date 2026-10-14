package com.acme.transport.lastmile.locker.window

/** How screens and exports show a locker window. */
data class LockerWindowView(
    var code: String,
    var name: String,
    var floorValueText: String,
    var driverIdText: String,
    var durationMinutes: String,
    var categoryText: String,
    var amountText: String,
    var statusLabel: String,
)

fun toLockerWindowView(item: LockerWindow): LockerWindowView {
    return LockerWindowView(
        code = item.code,
        name = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        driverIdText = item.driverId,
        durationMinutes = item.durationMinutes.toString(),
        categoryText = item.category,
        amountText = "%.2f".format(item.amount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerWindowViews(items: MutableList<LockerWindow>): MutableList<LockerWindowView> {
    val views = mutableListOf<LockerWindowView>()
    for (item in items) {
        views.add(toLockerWindowView(item))
    }
    return views
}

fun sumLockerWindowView(view: LockerWindowView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedFloorValue + parsedDurationMinutes.toDouble() + parsedAmount
}
