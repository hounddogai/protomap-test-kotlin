package com.acme.transport.linehaul.trailer.trailerpreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional trailer preference. */
data class RegionalTrailerPreferenceView(
    var codeText: String,
    var title: String,
    var pallets: String,
    var attempts: String,
    var vehicleIdText: String,
    var region: String,
    var sequence: String,
    var checksum: String,
    var statusLabel: String,
)

fun toRegionalTrailerPreferenceView(item: RegionalTrailerPreference): RegionalTrailerPreferenceView {
    return RegionalTrailerPreferenceView(
        codeText = item.code,
        title = item.title,
        pallets = item.pallets.toString(),
        attempts = item.attempts.toString(),
        vehicleIdText = item.vehicleId,
        region = truncate(item.region, 64),
        sequence = item.sequence.toString(),
        checksum = truncate(item.checksum, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTrailerPreferenceViews(items: MutableList<RegionalTrailerPreference>): MutableList<RegionalTrailerPreferenceView> {
    val views = mutableListOf<RegionalTrailerPreferenceView>()
    for (item in items) {
        views.add(toRegionalTrailerPreferenceView(item))
    }
    return views
}

fun sumRegionalTrailerPreferenceView(view: RegionalTrailerPreferenceView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedAttempts = view.attempts.toInt()
    val parsedSequence = view.sequence.toInt()
    return parsedPallets.toDouble() + parsedAttempts.toDouble() + parsedSequence.toDouble()
}
