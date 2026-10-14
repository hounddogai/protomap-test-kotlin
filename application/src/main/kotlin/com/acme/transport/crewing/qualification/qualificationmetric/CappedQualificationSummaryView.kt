package com.acme.transport.crewing.qualification.qualificationmetric

/** How screens and exports show a capped qualification summary. */
data class CappedQualificationSummaryView(
    var codeText: String,
    var nameText: String,
    var createdAtText: String,
    var updatedAtText: String,
    var routeCode: String,
    var notes: String,
    var reviewer: String,
    var description: String,
    var channelText: String,
    var statusLabel: String,
)

fun toCappedQualificationSummaryView(item: CappedQualificationSummary): CappedQualificationSummaryView {
    return CappedQualificationSummaryView(
        codeText = item.code,
        nameText = item.name,
        createdAtText = item.createdAt.toString(),
        updatedAtText = item.updatedAt.toString(),
        routeCode = item.routeCode,
        notes = item.notes,
        reviewer = item.reviewer,
        description = item.description,
        channelText = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedQualificationSummaryViews(items: MutableList<CappedQualificationSummary>): MutableList<CappedQualificationSummaryView> {
    val views = mutableListOf<CappedQualificationSummaryView>()
    for (item in items) {
        views.add(toCappedQualificationSummaryView(item))
    }
    return views
}

fun sumCappedQualificationSummaryView(view: CappedQualificationSummaryView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble()
}
