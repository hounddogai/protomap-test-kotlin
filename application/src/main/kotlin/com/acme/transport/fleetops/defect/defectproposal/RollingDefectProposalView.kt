package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling defect proposal. */
data class RollingDefectProposalView(
    var code: String,
    var name: String,
    var distanceKmText: String,
    var stopCountText: String,
    var attempts: String,
    var routeCodeText: String,
    var externalRefText: String,
    var phoneText: String,
    var amountText: String,
    var approved: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toRollingDefectProposalView(item: RollingDefectProposal): RollingDefectProposalView {
    return RollingDefectProposalView(
        code = item.code,
        name = item.name,
        distanceKmText = "%.2f".format(item.distanceKm),
        stopCountText = item.stopCount.toString(),
        attempts = item.attempts.toString(),
        routeCodeText = truncate(item.routeCode, 80),
        externalRefText = item.externalRef,
        phoneText = item.phone,
        amountText = "%.2f".format(item.amount),
        approved = "${item.approved}",
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingDefectProposalViews(items: MutableList<RollingDefectProposal>): MutableList<RollingDefectProposalView> {
    val views = mutableListOf<RollingDefectProposalView>()
    for (item in items) {
        views.add(toRollingDefectProposalView(item))
    }
    return views
}

fun sumRollingDefectProposalView(view: RollingDefectProposalView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedDistanceKm + parsedStopCount.toDouble() + parsedAttempts.toDouble()
}
