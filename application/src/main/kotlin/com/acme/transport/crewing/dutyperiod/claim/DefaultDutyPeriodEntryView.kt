package com.acme.transport.crewing.dutyperiod.claim

/** How screens and exports show a default duty period entry. */
data class DefaultDutyPeriodEntryView(
    var code: String,
    var label: String,
    var region: String,
    var revision: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toDefaultDutyPeriodEntryView(item: DefaultDutyPeriodEntry): DefaultDutyPeriodEntryView {
    return DefaultDutyPeriodEntryView(
        code = item.code,
        label = item.label,
        region = item.region,
        revision = item.revision.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultDutyPeriodEntryViews(items: MutableList<DefaultDutyPeriodEntry>): MutableList<DefaultDutyPeriodEntryView> {
    val views = mutableListOf<DefaultDutyPeriodEntryView>()
    for (item in items) {
        views.add(toDefaultDutyPeriodEntryView(item))
    }
    return views
}

fun sumDefaultDutyPeriodEntryView(view: DefaultDutyPeriodEntryView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
