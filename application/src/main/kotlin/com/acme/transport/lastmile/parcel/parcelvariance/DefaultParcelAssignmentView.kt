package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default parcel assignment. */
data class DefaultParcelAssignmentView(
    var code: String,
    var name: String,
    var cost: String,
    var country: String,
    var routeCodeText: String,
    var driverIdText: String,
    var channelText: String,
    var ownerId: String,
    var priorityText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toDefaultParcelAssignmentView(item: DefaultParcelAssignment): DefaultParcelAssignmentView {
    return DefaultParcelAssignmentView(
        code = item.code,
        name = item.name,
        cost = "%.2f".format(item.cost),
        country = item.country,
        routeCodeText = item.routeCode,
        driverIdText = item.driverId,
        channelText = truncate(item.channel, 80),
        ownerId = item.ownerId,
        priorityText = item.priority.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultParcelAssignmentViews(items: MutableList<DefaultParcelAssignment>): MutableList<DefaultParcelAssignmentView> {
    val views = mutableListOf<DefaultParcelAssignmentView>()
    for (item in items) {
        views.add(toDefaultParcelAssignmentView(item))
    }
    return views
}

fun sumDefaultParcelAssignmentView(view: DefaultParcelAssignmentView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedPriority = view.priorityText.toInt()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCost + parsedPriority.toDouble() + parsedMinimum
}
