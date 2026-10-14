package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending locker audit. */
data class PendingLockerAuditView(
    var code: String,
    var title: String,
    var stopCount: String,
    var phone: String,
    var category: String,
    var statusLabel: String,
)

fun toPendingLockerAuditView(item: PendingLockerAudit): PendingLockerAuditView {
    return PendingLockerAuditView(
        code = item.code,
        title = truncate(item.title, 120),
        stopCount = item.stopCount.toString(),
        phone = truncate(item.phone, 64),
        category = truncate(item.category, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLockerAuditViews(items: MutableList<PendingLockerAudit>): MutableList<PendingLockerAuditView> {
    val views = mutableListOf<PendingLockerAuditView>()
    for (item in items) {
        views.add(toPendingLockerAuditView(item))
    }
    return views
}

fun sumPendingLockerAuditView(view: PendingLockerAuditView): Double {
    val parsedStopCount = view.stopCount.toInt()
    return parsedStopCount.toDouble()
}
