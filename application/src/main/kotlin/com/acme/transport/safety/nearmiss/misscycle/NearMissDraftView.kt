package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a near miss draft. */
data class NearMissDraftView(
    var codeText: String,
    var nameText: String,
    var retries: String,
    var routeCodeText: String,
    var createdAtText: String,
    var stopCountText: String,
    var category: String,
    var expiresAtText: String,
    var vehicleId: String,
    var phoneText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toNearMissDraftView(item: NearMissDraft): NearMissDraftView {
    return NearMissDraftView(
        codeText = item.code,
        nameText = item.name,
        retries = item.retries.toString(),
        routeCodeText = item.routeCode,
        createdAtText = item.createdAt.toString(),
        stopCountText = item.stopCount.toString(),
        category = truncate(item.category, 120),
        expiresAtText = item.expiresAt.toString(),
        vehicleId = item.vehicleId,
        phoneText = item.phone,
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNearMissDraftViews(items: MutableList<NearMissDraft>): MutableList<NearMissDraftView> {
    val views = mutableListOf<NearMissDraftView>()
    for (item in items) {
        views.add(toNearMissDraftView(item))
    }
    return views
}

fun sumNearMissDraftView(view: NearMissDraftView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedRetries.toDouble() + parsedCreatedAt.toDouble() + parsedStopCount.toDouble()
}
