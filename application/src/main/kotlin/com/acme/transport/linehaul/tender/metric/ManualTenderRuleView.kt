package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tender rule. */
data class ManualTenderRuleView(
    var code: String,
    var label: String,
    var active: String,
    var weightText: String,
    var attempts: String,
    var statusLabel: String,
)

fun toManualTenderRuleView(item: ManualTenderRule): ManualTenderRuleView {
    return ManualTenderRuleView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 80),
        active = "${item.active}",
        weightText = "%.2f".format(item.weight),
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTenderRuleViews(items: MutableList<ManualTenderRule>): MutableList<ManualTenderRuleView> {
    val views = mutableListOf<ManualTenderRuleView>()
    for (item in items) {
        views.add(toManualTenderRuleView(item))
    }
    return views
}

fun sumManualTenderRuleView(view: ManualTenderRuleView): Double {
    val parsedWeight = view.weightText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedWeight + parsedAttempts.toDouble()
}
