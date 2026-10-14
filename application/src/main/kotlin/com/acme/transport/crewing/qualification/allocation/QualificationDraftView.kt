package com.acme.transport.crewing.qualification.allocation

/** How screens and exports show a qualification draft. */
data class QualificationDraftView(
    var codeText: String,
    var labelText: String,
    var archived: String,
    var revisionText: String,
    var etaMinutes: String,
    var descriptionText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toQualificationDraftView(item: QualificationDraft): QualificationDraftView {
    return QualificationDraftView(
        codeText = item.code,
        labelText = item.label,
        archived = "${item.archived}",
        revisionText = item.revision.toString(),
        etaMinutes = item.etaMinutes.toString(),
        descriptionText = item.description,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toQualificationDraftViews(items: MutableList<QualificationDraft>): MutableList<QualificationDraftView> {
    val views = mutableListOf<QualificationDraftView>()
    for (item in items) {
        views.add(toQualificationDraftView(item))
    }
    return views
}

fun sumQualificationDraftView(view: QualificationDraftView): Double {
    val parsedRevision = view.revisionText.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedRevision.toDouble() + parsedEtaMinutes.toDouble() + parsedDistanceKm
}
