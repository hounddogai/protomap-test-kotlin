package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling attempt exception. */
data class RollingAttemptExceptionView(
    var code: String,
    var name: String,
    var odometer: String,
    var contactEmail: String,
    var loadKgText: String,
    var shiftCode: String,
    var limit: String,
    var visible: String,
    var category: String,
    var ownerId: String,
    var pinnedText: String,
    var statusLabel: String,
)

fun toRollingAttemptExceptionView(item: RollingAttemptException): RollingAttemptExceptionView {
    return RollingAttemptExceptionView(
        code = truncate(item.code, 120),
        name = item.name,
        odometer = item.odometer.toString(),
        contactEmail = item.contactEmail,
        loadKgText = "%.2f".format(item.loadKg),
        shiftCode = item.shiftCode,
        limit = item.limit.toString(),
        visible = "${item.visible}",
        category = truncate(item.category, 80),
        ownerId = item.ownerId,
        pinnedText = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAttemptExceptionViews(items: MutableList<RollingAttemptException>): MutableList<RollingAttemptExceptionView> {
    val views = mutableListOf<RollingAttemptExceptionView>()
    for (item in items) {
        views.add(toRollingAttemptExceptionView(item))
    }
    return views
}

fun sumRollingAttemptExceptionView(view: RollingAttemptExceptionView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedOdometer.toDouble() + parsedLoadKg + parsedLimit.toDouble()
}
