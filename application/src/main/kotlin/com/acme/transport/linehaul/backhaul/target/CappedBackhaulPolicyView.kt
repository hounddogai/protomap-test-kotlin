package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped backhaul policy. */
data class CappedBackhaulPolicyView(
    var code: String,
    var titleText: String,
    var budgetText: String,
    var localeText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toCappedBackhaulPolicyView(item: CappedBackhaulPolicy): CappedBackhaulPolicyView {
    return CappedBackhaulPolicyView(
        code = item.code,
        titleText = item.title,
        budgetText = "%.2f".format(item.budget),
        localeText = truncate(item.locale, 64),
        shiftCode = truncate(item.shiftCode, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBackhaulPolicyViews(items: MutableList<CappedBackhaulPolicy>): MutableList<CappedBackhaulPolicyView> {
    val views = mutableListOf<CappedBackhaulPolicyView>()
    for (item in items) {
        views.add(toCappedBackhaulPolicyView(item))
    }
    return views
}

fun sumCappedBackhaulPolicyView(view: CappedBackhaulPolicyView): Double {
    val parsedBudget = view.budgetText.toDouble()
    return parsedBudget
}
