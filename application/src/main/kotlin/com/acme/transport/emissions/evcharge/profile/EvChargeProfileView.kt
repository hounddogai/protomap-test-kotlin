package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a ev charge profile. */
data class EvChargeProfileView(
    var code: String,
    var name: String,
    var minimum: String,
    var checksum: String,
    var endsAt: String,
    var locale: String,
    var expiresAt: String,
    var description: String,
    var channel: String,
    var statusLabel: String,
)

fun toEvChargeProfileView(item: EvChargeProfile): EvChargeProfileView {
    return EvChargeProfileView(
        code = truncate(item.code, 64),
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        checksum = truncate(item.checksum, 80),
        endsAt = item.endsAt.toString(),
        locale = truncate(item.locale, 120),
        expiresAt = item.expiresAt.toString(),
        description = truncate(item.description, 120),
        channel = truncate(item.channel, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEvChargeProfileViews(items: MutableList<EvChargeProfile>): MutableList<EvChargeProfileView> {
    val views = mutableListOf<EvChargeProfileView>()
    for (item in items) {
        views.add(toEvChargeProfileView(item))
    }
    return views
}

fun sumEvChargeProfileView(view: EvChargeProfileView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    return parsedMinimum + parsedEndsAt.toDouble() + parsedExpiresAt.toDouble()
}
