package com.acme.transport.safety.coaching.milestone

/** How screens and exports show a weighted coaching template. */
data class WeightedCoachingTemplateView(
    var codeText: String,
    var name: String,
    var durationMinutes: String,
    var scoreText: String,
    var checksum: String,
    var version: String,
    var externalRef: String,
    var source: String,
    var weight: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toWeightedCoachingTemplateView(item: WeightedCoachingTemplate): WeightedCoachingTemplateView {
    return WeightedCoachingTemplateView(
        codeText = item.code,
        name = item.name,
        durationMinutes = item.durationMinutes.toString(),
        scoreText = "%.2f".format(item.score),
        checksum = item.checksum,
        version = item.version.toString(),
        externalRef = item.externalRef,
        source = item.source,
        weight = "%.2f".format(item.weight),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedCoachingTemplateViews(items: MutableList<WeightedCoachingTemplate>): MutableList<WeightedCoachingTemplateView> {
    val views = mutableListOf<WeightedCoachingTemplateView>()
    for (item in items) {
        views.add(toWeightedCoachingTemplateView(item))
    }
    return views
}

fun sumWeightedCoachingTemplateView(view: WeightedCoachingTemplateView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedScore = view.scoreText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedDurationMinutes.toDouble() + parsedScore + parsedVersion.toDouble()
}
