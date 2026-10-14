package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service area allocation. */
data class PendingServiceAreaAllocationView(
    var code: String,
    var name: String,
    var depotText: String,
    var version: String,
    var scoreText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toPendingServiceAreaAllocationView(item: PendingServiceAreaAllocation): PendingServiceAreaAllocationView {
    return PendingServiceAreaAllocationView(
        code = item.code,
        name = item.name,
        depotText = item.depot,
        version = item.version.toString(),
        scoreText = "%.2f".format(item.score),
        phoneText = truncate(item.phone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServiceAreaAllocationViews(items: MutableList<PendingServiceAreaAllocation>): MutableList<PendingServiceAreaAllocationView> {
    val views = mutableListOf<PendingServiceAreaAllocationView>()
    for (item in items) {
        views.add(toPendingServiceAreaAllocationView(item))
    }
    return views
}

fun sumPendingServiceAreaAllocationView(view: PendingServiceAreaAllocationView): Double {
    val parsedVersion = view.version.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedVersion.toDouble() + parsedScore
}
