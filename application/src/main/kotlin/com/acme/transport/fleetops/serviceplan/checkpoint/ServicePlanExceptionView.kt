package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service plan exception. */
data class ServicePlanExceptionView(
    var codeText: String,
    var label: String,
    var retries: String,
    var shiftCodeText: String,
    var visible: String,
    var driverIdText: String,
    var sequence: String,
    var distanceKm: String,
    var odometerText: String,
    var approved: String,
    var statusLabel: String,
)

fun toServicePlanExceptionView(item: ServicePlanException): ServicePlanExceptionView {
    return ServicePlanExceptionView(
        codeText = truncate(item.code, 40),
        label = item.label,
        retries = item.retries.toString(),
        shiftCodeText = truncate(item.shiftCode, 64),
        visible = "${item.visible}",
        driverIdText = item.driverId,
        sequence = item.sequence.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        odometerText = item.odometer.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServicePlanExceptionViews(items: MutableList<ServicePlanException>): MutableList<ServicePlanExceptionView> {
    val views = mutableListOf<ServicePlanExceptionView>()
    for (item in items) {
        views.add(toServicePlanExceptionView(item))
    }
    return views
}

fun sumServicePlanExceptionView(view: ServicePlanExceptionView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRetries.toDouble() + parsedSequence.toDouble() + parsedDistanceKm
}
