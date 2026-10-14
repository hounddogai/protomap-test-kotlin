package com.acme.transport.fleetops.leasereturn.returntier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient lease return manifest. */
data class LenientLeaseReturnManifestView(
    var codeText: String,
    var titleText: String,
    var limitText: String,
    var dock: String,
    var label: String,
    var reviewerText: String,
    var rate: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toLenientLeaseReturnManifestView(item: LenientLeaseReturnManifest): LenientLeaseReturnManifestView {
    return LenientLeaseReturnManifestView(
        codeText = item.code,
        titleText = item.title,
        limitText = item.limit.toString(),
        dock = item.dock,
        label = item.label,
        reviewerText = item.reviewer,
        rate = "%.2f".format(item.rate),
        externalRef = truncate(item.externalRef, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientLeaseReturnManifestViews(items: MutableList<LenientLeaseReturnManifest>): MutableList<LenientLeaseReturnManifestView> {
    val views = mutableListOf<LenientLeaseReturnManifestView>()
    for (item in items) {
        views.add(toLenientLeaseReturnManifestView(item))
    }
    return views
}

fun sumLenientLeaseReturnManifestView(view: LenientLeaseReturnManifestView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedLimit.toDouble() + parsedRate
}
