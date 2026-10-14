package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification window. */
data class QualificationWindowView(
    var code: String,
    var name: String,
    var unit: String,
    var sequence: String,
    var countryText: String,
    var limit: String,
    var budget: String,
    var approved: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toQualificationWindowView(item: QualificationWindow): QualificationWindowView {
    return QualificationWindowView(
        code = item.code,
        name = item.name,
        unit = item.unit,
        sequence = item.sequence.toString(),
        countryText = truncate(item.country, 120),
        limit = item.limit.toString(),
        budget = "%.2f".format(item.budget),
        approved = "${item.approved}",
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationWindowViews(items: MutableList<QualificationWindow>): MutableList<QualificationWindowView> {
    val views = mutableListOf<QualificationWindowView>()
    for (item in items) {
        views.add(toQualificationWindowView(item))
    }
    return views
}

fun sumQualificationWindowView(view: QualificationWindowView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedSequence.toDouble() + parsedLimit.toDouble() + parsedBudget
}
