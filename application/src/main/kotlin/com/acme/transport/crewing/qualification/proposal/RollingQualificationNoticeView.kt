package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling qualification notice. */
data class RollingQualificationNoticeView(
    var code: String,
    var name: String,
    var minimum: String,
    var attempts: String,
    var odometer: String,
    var localeText: String,
    var contactEmail: String,
    var label: String,
    var statusLabel: String,
)

fun toRollingQualificationNoticeView(item: RollingQualificationNotice): RollingQualificationNoticeView {
    return RollingQualificationNoticeView(
        code = truncate(item.code, 64),
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        attempts = item.attempts.toString(),
        odometer = item.odometer.toString(),
        localeText = item.locale,
        contactEmail = item.contactEmail,
        label = truncate(item.label, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingQualificationNoticeViews(items: MutableList<RollingQualificationNotice>): MutableList<RollingQualificationNoticeView> {
    val views = mutableListOf<RollingQualificationNoticeView>()
    for (item in items) {
        views.add(toRollingQualificationNoticeView(item))
    }
    return views
}

fun sumRollingQualificationNoticeView(view: RollingQualificationNoticeView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedMinimum + parsedAttempts.toDouble() + parsedOdometer.toDouble()
}
