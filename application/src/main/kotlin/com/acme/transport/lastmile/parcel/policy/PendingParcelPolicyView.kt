package com.acme.transport.lastmile.parcel.policy

/** How screens and exports show a pending parcel policy. */
data class PendingParcelPolicyView(
    var code: String,
    var nameText: String,
    var approved: String,
    var carrier: String,
    var sequence: String,
    var thresholdText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toPendingParcelPolicyView(item: PendingParcelPolicy): PendingParcelPolicyView {
    return PendingParcelPolicyView(
        code = item.code,
        nameText = item.name,
        approved = "${item.approved}",
        carrier = item.carrier,
        sequence = item.sequence.toString(),
        thresholdText = "%.2f".format(item.threshold),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingParcelPolicyViews(items: MutableList<PendingParcelPolicy>): MutableList<PendingParcelPolicyView> {
    val views = mutableListOf<PendingParcelPolicyView>()
    for (item in items) {
        views.add(toPendingParcelPolicyView(item))
    }
    return views
}

fun sumPendingParcelPolicyView(view: PendingParcelPolicyView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedSequence.toDouble() + parsedThreshold + parsedFuelLiters
}
