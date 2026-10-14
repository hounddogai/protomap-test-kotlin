package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped locker manifest. */
data class CappedLockerManifestView(
    var code: String,
    var name: String,
    var threshold: String,
    var weight: String,
    var loadKgText: String,
    var localeText: String,
    var cost: String,
    var statusLabel: String,
)

fun toCappedLockerManifestView(item: CappedLockerManifest): CappedLockerManifestView {
    return CappedLockerManifestView(
        code = item.code,
        name = truncate(item.name, 40),
        threshold = "%.2f".format(item.threshold),
        weight = "%.2f".format(item.weight),
        loadKgText = "%.2f".format(item.loadKg),
        localeText = item.locale,
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedLockerManifestViews(items: MutableList<CappedLockerManifest>): MutableList<CappedLockerManifestView> {
    val views = mutableListOf<CappedLockerManifestView>()
    for (item in items) {
        views.add(toCappedLockerManifestView(item))
    }
    return views
}

fun sumCappedLockerManifestView(view: CappedLockerManifestView): Double {
    val parsedThreshold = view.threshold.toDouble()
    val parsedWeight = view.weight.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    return parsedThreshold + parsedWeight + parsedLoadKg
}
