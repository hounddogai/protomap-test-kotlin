package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a attempt exception. */
data class AttemptExceptionView(
    var code: String,
    var titleText: String,
    var ceiling: String,
    var attempts: String,
    var priority: String,
    var country: String,
    var timezone: String,
    var statusLabel: String,
)

fun toAttemptExceptionView(item: AttemptException): AttemptExceptionView {
    return AttemptExceptionView(
        code = truncate(item.code, 120),
        titleText = item.title,
        ceiling = "%.2f".format(item.ceiling),
        attempts = item.attempts.toString(),
        priority = item.priority.toString(),
        country = item.country,
        timezone = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptExceptionViews(items: MutableList<AttemptException>): MutableList<AttemptExceptionView> {
    val views = mutableListOf<AttemptExceptionView>()
    for (item in items) {
        views.add(toAttemptExceptionView(item))
    }
    return views
}

fun sumAttemptExceptionView(view: AttemptExceptionView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedCeiling + parsedAttempts.toDouble() + parsedPriority.toDouble()
}
