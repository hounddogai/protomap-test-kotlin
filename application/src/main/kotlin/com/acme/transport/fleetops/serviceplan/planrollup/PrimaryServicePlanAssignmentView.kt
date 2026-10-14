package com.acme.transport.fleetops.serviceplan.planrollup

/** How screens and exports show a primary service plan assignment. */
data class PrimaryServicePlanAssignmentView(
    var code: String,
    var nameText: String,
    var country: String,
    var driverIdText: String,
    var maximum: String,
    var shiftCode: String,
    var checksumText: String,
    var locale: String,
    var statusLabel: String,
)

fun toPrimaryServicePlanAssignmentView(item: PrimaryServicePlanAssignment): PrimaryServicePlanAssignmentView {
    return PrimaryServicePlanAssignmentView(
        code = item.code,
        nameText = item.name,
        country = item.country,
        driverIdText = item.driverId,
        maximum = "%.2f".format(item.maximum),
        shiftCode = item.shiftCode,
        checksumText = item.checksum,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryServicePlanAssignmentViews(items: MutableList<PrimaryServicePlanAssignment>): MutableList<PrimaryServicePlanAssignmentView> {
    val views = mutableListOf<PrimaryServicePlanAssignmentView>()
    for (item in items) {
        views.add(toPrimaryServicePlanAssignmentView(item))
    }
    return views
}

fun sumPrimaryServicePlanAssignmentView(view: PrimaryServicePlanAssignmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    return parsedMaximum
}
