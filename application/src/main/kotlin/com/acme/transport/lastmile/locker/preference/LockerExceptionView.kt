package com.acme.transport.lastmile.locker.preference

/** How screens and exports show a locker exception. */
data class LockerExceptionView(
    var code: String,
    var name: String,
    var stopCountText: String,
    var distanceKm: String,
    var carrier: String,
    var contactEmailText: String,
    var capacity: String,
    var priorityText: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toLockerExceptionView(item: LockerException): LockerExceptionView {
    return LockerExceptionView(
        code = item.code,
        name = item.name,
        stopCountText = item.stopCount.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        carrier = item.carrier,
        contactEmailText = item.contactEmail,
        capacity = item.capacity.toString(),
        priorityText = item.priority.toString(),
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerExceptionViews(items: MutableList<LockerException>): MutableList<LockerExceptionView> {
    val views = mutableListOf<LockerExceptionView>()
    for (item in items) {
        views.add(toLockerExceptionView(item))
    }
    return views
}

fun sumLockerExceptionView(view: LockerExceptionView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedStopCount.toDouble() + parsedDistanceKm + parsedCapacity.toDouble()
}
