package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global carbon report policy. */
data class GlobalCarbonReportPolicyView(
    var code: String,
    var nameText: String,
    var margin: String,
    var reviewer: String,
    var currency: String,
    var phone: String,
    var startsAt: String,
    var endsAt: String,
    var activeText: String,
    var priorityText: String,
    var statusLabel: String,
)

fun toGlobalCarbonReportPolicyView(item: GlobalCarbonReportPolicy): GlobalCarbonReportPolicyView {
    return GlobalCarbonReportPolicyView(
        code = truncate(item.code, 120),
        nameText = truncate(item.name, 120),
        margin = "%.2f".format(item.margin),
        reviewer = item.reviewer,
        currency = truncate(item.currency, 40),
        phone = item.phone,
        startsAt = item.startsAt.toString(),
        endsAt = item.endsAt.toString(),
        activeText = "${item.active}",
        priorityText = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalCarbonReportPolicyViews(items: MutableList<GlobalCarbonReportPolicy>): MutableList<GlobalCarbonReportPolicyView> {
    val views = mutableListOf<GlobalCarbonReportPolicyView>()
    for (item in items) {
        views.add(toGlobalCarbonReportPolicyView(item))
    }
    return views
}

fun sumGlobalCarbonReportPolicyView(view: GlobalCarbonReportPolicyView): Double {
    val parsedMargin = view.margin.toDouble()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedMargin + parsedStartsAt.toDouble() + parsedEndsAt.toDouble()
}
