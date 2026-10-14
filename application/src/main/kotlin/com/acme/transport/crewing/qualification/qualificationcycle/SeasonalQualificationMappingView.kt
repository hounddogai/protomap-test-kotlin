package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal qualification mapping. */
data class SeasonalQualificationMappingView(
    var code: String,
    var title: String,
    var labelText: String,
    var stopCountText: String,
    var sourceText: String,
    var maximum: String,
    var trailerIdText: String,
    var segment: String,
    var capacity: String,
    var statusLabel: String,
)

fun toSeasonalQualificationMappingView(item: SeasonalQualificationMapping): SeasonalQualificationMappingView {
    return SeasonalQualificationMappingView(
        code = truncate(item.code, 120),
        title = item.title,
        labelText = item.label,
        stopCountText = item.stopCount.toString(),
        sourceText = item.source,
        maximum = "%.2f".format(item.maximum),
        trailerIdText = item.trailerId,
        segment = item.segment,
        capacity = item.capacity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalQualificationMappingViews(items: MutableList<SeasonalQualificationMapping>): MutableList<SeasonalQualificationMappingView> {
    val views = mutableListOf<SeasonalQualificationMappingView>()
    for (item in items) {
        views.add(toSeasonalQualificationMappingView(item))
    }
    return views
}

fun sumSeasonalQualificationMappingView(view: SeasonalQualificationMappingView): Double {
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedStopCount.toDouble() + parsedMaximum + parsedCapacity.toDouble()
}
