package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal trailer yard override. */
data class SeasonalTrailerYardOverrideView(
    var codeText: String,
    var nameText: String,
    var channel: String,
    var etaMinutes: String,
    var versionText: String,
    var description: String,
    var ceiling: String,
    var visible: String,
    var notesText: String,
    var statusLabel: String,
)

fun toSeasonalTrailerYardOverrideView(item: SeasonalTrailerYardOverride): SeasonalTrailerYardOverrideView {
    return SeasonalTrailerYardOverrideView(
        codeText = item.code,
        nameText = truncate(item.name, 40),
        channel = item.channel,
        etaMinutes = item.etaMinutes.toString(),
        versionText = item.version.toString(),
        description = item.description,
        ceiling = "%.2f".format(item.ceiling),
        visible = "${item.visible}",
        notesText = truncate(item.notes, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalTrailerYardOverrideViews(items: MutableList<SeasonalTrailerYardOverride>): MutableList<SeasonalTrailerYardOverrideView> {
    val views = mutableListOf<SeasonalTrailerYardOverrideView>()
    for (item in items) {
        views.add(toSeasonalTrailerYardOverrideView(item))
    }
    return views
}

fun sumSeasonalTrailerYardOverrideView(view: SeasonalTrailerYardOverrideView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    return parsedEtaMinutes.toDouble() + parsedVersion.toDouble() + parsedCeiling
}
