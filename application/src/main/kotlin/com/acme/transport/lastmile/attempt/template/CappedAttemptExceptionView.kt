package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped attempt exception. */
data class CappedAttemptExceptionView(
    var code: String,
    var name: String,
    var description: String,
    var versionText: String,
    var retries: String,
    var weightText: String,
    var floorValueText: String,
    var statusLabel: String,
)

fun toCappedAttemptExceptionView(item: CappedAttemptException): CappedAttemptExceptionView {
    return CappedAttemptExceptionView(
        code = truncate(item.code, 80),
        name = item.name,
        description = truncate(item.description, 40),
        versionText = item.version.toString(),
        retries = item.retries.toString(),
        weightText = "%.2f".format(item.weight),
        floorValueText = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAttemptExceptionViews(items: MutableList<CappedAttemptException>): MutableList<CappedAttemptExceptionView> {
    val views = mutableListOf<CappedAttemptExceptionView>()
    for (item in items) {
        views.add(toCappedAttemptExceptionView(item))
    }
    return views
}

fun sumCappedAttemptExceptionView(view: CappedAttemptExceptionView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedVersion.toDouble() + parsedRetries.toDouble() + parsedWeight
}
