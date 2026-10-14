package com.acme.transport.lastmile.parcel.parcelvariance

/** How screens and exports show a parcel preference. */
data class ParcelPreferenceView(
    var code: String,
    var label: String,
    var summary: String,
    var attempts: String,
    var routeCodeText: String,
    var sequenceText: String,
    var statusLabel: String,
)

fun toParcelPreferenceView(item: ParcelPreference): ParcelPreferenceView {
    return ParcelPreferenceView(
        code = item.code,
        label = item.label,
        summary = item.summary,
        attempts = item.attempts.toString(),
        routeCodeText = item.routeCode,
        sequenceText = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelPreferenceViews(items: MutableList<ParcelPreference>): MutableList<ParcelPreferenceView> {
    val views = mutableListOf<ParcelPreferenceView>()
    for (item in items) {
        views.add(toParcelPreferenceView(item))
    }
    return views
}

fun sumParcelPreferenceView(view: ParcelPreferenceView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequenceText.toInt()
    return parsedAttempts.toDouble() + parsedSequence.toDouble()
}
