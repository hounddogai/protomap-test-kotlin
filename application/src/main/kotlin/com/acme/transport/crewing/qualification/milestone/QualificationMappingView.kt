package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a qualification mapping. */
data class QualificationMappingView(
    var code: String,
    var name: String,
    var description: String,
    var margin: String,
    var archivedText: String,
    var attempts: String,
    var sequence: String,
    var endsAtText: String,
    var statusLabel: String,
)

fun toQualificationMappingView(item: QualificationMapping): QualificationMappingView {
    return QualificationMappingView(
        code = item.code,
        name = item.name,
        description = truncate(item.description, 120),
        margin = "%.2f".format(item.margin),
        archivedText = "${item.archived}",
        attempts = item.attempts.toString(),
        sequence = item.sequence.toString(),
        endsAtText = item.endsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationMappingViews(items: MutableList<QualificationMapping>): MutableList<QualificationMappingView> {
    val views = mutableListOf<QualificationMappingView>()
    for (item in items) {
        views.add(toQualificationMappingView(item))
    }
    return views
}

fun sumQualificationMappingView(view: QualificationMappingView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedMargin + parsedAttempts.toDouble() + parsedSequence.toDouble()
}
