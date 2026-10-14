package com.acme.transport.linehaul.capacity.request

/** How screens and exports show a capacity request. */
data class CapacityRequestView(
    var codeText: String,
    var title: String,
    var palletsText: String,
    var vehicleIdText: String,
    var attemptsText: String,
    var stopCountText: String,
    var category: String,
    var depot: String,
    var durationMinutes: String,
    var endsAt: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toCapacityRequestView(item: CapacityRequest): CapacityRequestView {
    return CapacityRequestView(
        codeText = item.code,
        title = item.title,
        palletsText = item.pallets.toString(),
        vehicleIdText = item.vehicleId,
        attemptsText = item.attempts.toString(),
        stopCountText = item.stopCount.toString(),
        category = item.category,
        depot = item.depot,
        durationMinutes = item.durationMinutes.toString(),
        endsAt = item.endsAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityRequestViews(items: MutableList<CapacityRequest>): MutableList<CapacityRequestView> {
    val views = mutableListOf<CapacityRequestView>()
    for (item in items) {
        views.add(toCapacityRequestView(item))
    }
    return views
}

fun sumCapacityRequestView(view: CapacityRequestView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedPallets.toDouble() + parsedAttempts.toDouble() + parsedStopCount.toDouble()
}
