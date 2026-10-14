package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a locker variance. */
data class LockerVarianceView(
    var codeText: String,
    var title: String,
    var createdAtText: String,
    var expiresAt: String,
    var limit: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLockerVarianceView(item: LockerVariance): LockerVarianceView {
    return LockerVarianceView(
        codeText = item.code,
        title = truncate(item.title, 120),
        createdAtText = item.createdAt.toString(),
        expiresAt = item.expiresAt.toString(),
        limit = item.limit.toString(),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerVarianceViews(items: MutableList<LockerVariance>): MutableList<LockerVarianceView> {
    val views = mutableListOf<LockerVarianceView>()
    for (item in items) {
        views.add(toLockerVarianceView(item))
    }
    return views
}

fun sumLockerVarianceView(view: LockerVarianceView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedCreatedAt.toDouble() + parsedExpiresAt.toDouble() + parsedLimit.toDouble()
}
