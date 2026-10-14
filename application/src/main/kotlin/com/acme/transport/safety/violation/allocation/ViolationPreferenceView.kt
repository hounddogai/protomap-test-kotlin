package com.acme.transport.safety.violation.allocation

/** How screens and exports show a violation preference. */
data class ViolationPreferenceView(
    var code: String,
    var title: String,
    var segment: String,
    var loadKgText: String,
    var sequence: String,
    var statusLabel: String,
)

fun toViolationPreferenceView(item: ViolationPreference): ViolationPreferenceView {
    return ViolationPreferenceView(
        code = item.code,
        title = item.title,
        segment = item.segment,
        loadKgText = "%.2f".format(item.loadKg),
        sequence = item.sequence.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationPreferenceViews(items: MutableList<ViolationPreference>): MutableList<ViolationPreferenceView> {
    val views = mutableListOf<ViolationPreferenceView>()
    for (item in items) {
        views.add(toViolationPreferenceView(item))
    }
    return views
}

fun sumViolationPreferenceView(view: ViolationPreferenceView): Double {
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedLoadKg + parsedSequence.toDouble()
}
