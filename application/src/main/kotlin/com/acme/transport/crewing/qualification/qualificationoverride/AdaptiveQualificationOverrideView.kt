package com.acme.transport.crewing.qualification.qualificationoverride

/** How screens and exports show a adaptive qualification override. */
data class AdaptiveQualificationOverrideView(
    var codeText: String,
    var title: String,
    var lane: String,
    var routeCode: String,
    var retries: String,
    var vehicleId: String,
    var threshold: String,
    var dockText: String,
    var statusLabel: String,
)

fun toAdaptiveQualificationOverrideView(item: AdaptiveQualificationOverride): AdaptiveQualificationOverrideView {
    return AdaptiveQualificationOverrideView(
        codeText = item.code,
        title = item.title,
        lane = item.lane,
        routeCode = item.routeCode,
        retries = item.retries.toString(),
        vehicleId = item.vehicleId,
        threshold = "%.2f".format(item.threshold),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveQualificationOverrideViews(items: MutableList<AdaptiveQualificationOverride>): MutableList<AdaptiveQualificationOverrideView> {
    val views = mutableListOf<AdaptiveQualificationOverrideView>()
    for (item in items) {
        views.add(toAdaptiveQualificationOverrideView(item))
    }
    return views
}

fun sumAdaptiveQualificationOverrideView(view: AdaptiveQualificationOverrideView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRetries.toDouble() + parsedThreshold
}
