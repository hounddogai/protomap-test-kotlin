package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted rest break override. */
data class WeightedRestBreakOverrideView(
    var codeText: String,
    var labelText: String,
    var attempts: String,
    var channel: String,
    var palletsText: String,
    var dockText: String,
    var regionText: String,
    var checksumText: String,
    var rateText: String,
    var statusLabel: String,
)

fun toWeightedRestBreakOverrideView(item: WeightedRestBreakOverride): WeightedRestBreakOverrideView {
    return WeightedRestBreakOverrideView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        attempts = item.attempts.toString(),
        channel = truncate(item.channel, 120),
        palletsText = item.pallets.toString(),
        dockText = item.dock,
        regionText = item.region,
        checksumText = item.checksum,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRestBreakOverrideViews(items: MutableList<WeightedRestBreakOverride>): MutableList<WeightedRestBreakOverrideView> {
    val views = mutableListOf<WeightedRestBreakOverrideView>()
    for (item in items) {
        views.add(toWeightedRestBreakOverrideView(item))
    }
    return views
}

fun sumWeightedRestBreakOverrideView(view: WeightedRestBreakOverrideView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedPallets = view.palletsText.toInt()
    val parsedRate = view.rateText.toDouble()
    return parsedAttempts.toDouble() + parsedPallets.toDouble() + parsedRate
}
