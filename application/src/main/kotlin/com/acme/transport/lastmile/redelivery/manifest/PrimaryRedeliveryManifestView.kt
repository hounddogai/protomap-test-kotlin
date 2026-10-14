package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary redelivery manifest. */
data class PrimaryRedeliveryManifestView(
    var code: String,
    var title: String,
    var maximum: String,
    var graceDays: String,
    var externalRefText: String,
    var description: String,
    var endsAt: String,
    var channelText: String,
    var statusLabel: String,
)

fun toPrimaryRedeliveryManifestView(item: PrimaryRedeliveryManifest): PrimaryRedeliveryManifestView {
    return PrimaryRedeliveryManifestView(
        code = item.code,
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        graceDays = item.graceDays.toString(),
        externalRefText = item.externalRef,
        description = item.description,
        endsAt = item.endsAt.toString(),
        channelText = truncate(item.channel, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryRedeliveryManifestViews(items: MutableList<PrimaryRedeliveryManifest>): MutableList<PrimaryRedeliveryManifestView> {
    val views = mutableListOf<PrimaryRedeliveryManifestView>()
    for (item in items) {
        views.add(toPrimaryRedeliveryManifestView(item))
    }
    return views
}

fun sumPrimaryRedeliveryManifestView(view: PrimaryRedeliveryManifestView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedMaximum + parsedGraceDays.toDouble() + parsedEndsAt.toDouble()
}
