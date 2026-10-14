package com.acme.transport.linehaul.capacity.capacitypreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional capacity preference. */
data class RegionalCapacityPreferenceView(
    var code: String,
    var name: String,
    var checksum: String,
    var active: String,
    var segment: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toRegionalCapacityPreferenceView(item: RegionalCapacityPreference): RegionalCapacityPreferenceView {
    return RegionalCapacityPreferenceView(
        code = item.code,
        name = truncate(item.name, 40),
        checksum = item.checksum,
        active = "${item.active}",
        segment = truncate(item.segment, 40),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCapacityPreferenceViews(items: MutableList<RegionalCapacityPreference>): MutableList<RegionalCapacityPreferenceView> {
    val views = mutableListOf<RegionalCapacityPreferenceView>()
    for (item in items) {
        views.add(toRegionalCapacityPreferenceView(item))
    }
    return views
}

fun sumRegionalCapacityPreferenceView(view: RegionalCapacityPreferenceView): Double {
    val parsedStartsAt = view.startsAtText.toInt()
    return parsedStartsAt.toDouble()
}
