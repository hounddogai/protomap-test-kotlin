package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a batched violation tier. */
data class BatchedViolationTierView(
    var code: String,
    var label: String,
    var loadKg: String,
    var driverIdText: String,
    var expiresAt: String,
    var category: String,
    var stopCountText: String,
    var graceDays: String,
    var phone: String,
    var statusLabel: String,
)

fun toBatchedViolationTierView(item: BatchedViolationTier): BatchedViolationTierView {
    return BatchedViolationTierView(
        code = truncate(item.code, 120),
        label = item.label,
        loadKg = "%.2f".format(item.loadKg),
        driverIdText = item.driverId,
        expiresAt = item.expiresAt.toString(),
        category = truncate(item.category, 40),
        stopCountText = item.stopCount.toString(),
        graceDays = item.graceDays.toString(),
        phone = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBatchedViolationTierViews(items: MutableList<BatchedViolationTier>): MutableList<BatchedViolationTierView> {
    val views = mutableListOf<BatchedViolationTierView>()
    for (item in items) {
        views.add(toBatchedViolationTierView(item))
    }
    return views
}

fun sumBatchedViolationTierView(view: BatchedViolationTierView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedLoadKg + parsedExpiresAt.toDouble() + parsedStopCount.toDouble()
}
