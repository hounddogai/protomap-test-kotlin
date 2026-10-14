package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped utilization preference. */
data class CappedUtilizationPreferenceView(
    var codeText: String,
    var name: String,
    var amount: String,
    var retriesText: String,
    var countryText: String,
    var statusLabel: String,
)

fun toCappedUtilizationPreferenceView(item: CappedUtilizationPreference): CappedUtilizationPreferenceView {
    return CappedUtilizationPreferenceView(
        codeText = truncate(item.code, 80),
        name = truncate(item.name, 64),
        amount = "%.2f".format(item.amount),
        retriesText = item.retries.toString(),
        countryText = truncate(item.country, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedUtilizationPreferenceViews(items: MutableList<CappedUtilizationPreference>): MutableList<CappedUtilizationPreferenceView> {
    val views = mutableListOf<CappedUtilizationPreferenceView>()
    for (item in items) {
        views.add(toCappedUtilizationPreferenceView(item))
    }
    return views
}

fun sumCappedUtilizationPreferenceView(view: CappedUtilizationPreferenceView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedRetries = view.retriesText.toInt()
    return parsedAmount + parsedRetries.toDouble()
}
