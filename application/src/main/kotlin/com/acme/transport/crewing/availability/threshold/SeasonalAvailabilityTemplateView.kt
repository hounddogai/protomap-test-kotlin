package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal availability template. */
data class SeasonalAvailabilityTemplateView(
    var codeText: String,
    var name: String,
    var segmentText: String,
    var revision: String,
    var ceilingText: String,
    var weightText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toSeasonalAvailabilityTemplateView(item: SeasonalAvailabilityTemplate): SeasonalAvailabilityTemplateView {
    return SeasonalAvailabilityTemplateView(
        codeText = truncate(item.code, 80),
        name = truncate(item.name, 120),
        segmentText = item.segment,
        revision = item.revision.toString(),
        ceilingText = "%.2f".format(item.ceiling),
        weightText = "%.2f".format(item.weight),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAvailabilityTemplateViews(items: MutableList<SeasonalAvailabilityTemplate>): MutableList<SeasonalAvailabilityTemplateView> {
    val views = mutableListOf<SeasonalAvailabilityTemplateView>()
    for (item in items) {
        views.add(toSeasonalAvailabilityTemplateView(item))
    }
    return views
}

fun sumSeasonalAvailabilityTemplateView(view: SeasonalAvailabilityTemplateView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    val parsedWeight = view.weightText.toDouble()
    return parsedRevision.toDouble() + parsedCeiling + parsedWeight
}
