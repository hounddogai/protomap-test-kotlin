package com.acme.transport.lastmile.locker.lockerprofile

/** How screens and exports show a fallback locker plan. */
data class FallbackLockerPlanView(
    var codeText: String,
    var title: String,
    var threshold: String,
    var maximumText: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toFallbackLockerPlanView(item: FallbackLockerPlan): FallbackLockerPlanView {
    return FallbackLockerPlanView(
        codeText = item.code,
        title = item.title,
        threshold = "%.2f".format(item.threshold),
        maximumText = "%.2f".format(item.maximum),
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLockerPlanViews(items: MutableList<FallbackLockerPlan>): MutableList<FallbackLockerPlanView> {
    val views = mutableListOf<FallbackLockerPlanView>()
    for (item in items) {
        views.add(toFallbackLockerPlanView(item))
    }
    return views
}

fun sumFallbackLockerPlanView(view: FallbackLockerPlanView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedThreshold + parsedMaximum + parsedFuelLiters
}
