package com.acme.transport.emissions.idling.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending idling manifest. */
data class PendingIdlingManifestView(
    var code: String,
    var label: String,
    var limitText: String,
    var revisionText: String,
    var createdAt: String,
    var category: String,
    var fuelLitersText: String,
    var statusLabel: String,
)

fun toPendingIdlingManifestView(item: PendingIdlingManifest): PendingIdlingManifestView {
    return PendingIdlingManifestView(
        code = item.code,
        label = truncate(item.label, 80),
        limitText = item.limit.toString(),
        revisionText = item.revision.toString(),
        createdAt = item.createdAt.toString(),
        category = item.category,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingIdlingManifestViews(items: MutableList<PendingIdlingManifest>): MutableList<PendingIdlingManifestView> {
    val views = mutableListOf<PendingIdlingManifestView>()
    for (item in items) {
        views.add(toPendingIdlingManifestView(item))
    }
    return views
}

fun sumPendingIdlingManifestView(view: PendingIdlingManifestView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedRevision = view.revisionText.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedLimit.toDouble() + parsedRevision.toDouble() + parsedCreatedAt.toDouble()
}
