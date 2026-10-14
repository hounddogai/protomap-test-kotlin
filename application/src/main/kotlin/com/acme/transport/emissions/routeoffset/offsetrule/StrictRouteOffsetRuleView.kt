package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict route offset rule. */
data class StrictRouteOffsetRuleView(
    var code: String,
    var labelText: String,
    var checksum: String,
    var marginText: String,
    var locale: String,
    var statusLabel: String,
)

fun toStrictRouteOffsetRuleView(item: StrictRouteOffsetRule): StrictRouteOffsetRuleView {
    return StrictRouteOffsetRuleView(
        code = item.code,
        labelText = truncate(item.label, 120),
        checksum = truncate(item.checksum, 80),
        marginText = "%.2f".format(item.margin),
        locale = truncate(item.locale, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictRouteOffsetRuleViews(items: MutableList<StrictRouteOffsetRule>): MutableList<StrictRouteOffsetRuleView> {
    val views = mutableListOf<StrictRouteOffsetRuleView>()
    for (item in items) {
        views.add(toStrictRouteOffsetRuleView(item))
    }
    return views
}

fun sumStrictRouteOffsetRuleView(view: StrictRouteOffsetRuleView): Double {
    val parsedMargin = view.marginText.toDouble()
    return parsedMargin
}
