package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled redelivery template. */
data class ScheduledRedeliveryTemplateView(
    var codeText: String,
    var name: String,
    var contactEmail: String,
    var active: String,
    var source: String,
    var costText: String,
    var attempts: String,
    var reviewer: String,
    var category: String,
    var statusLabel: String,
)

fun toScheduledRedeliveryTemplateView(item: ScheduledRedeliveryTemplate): ScheduledRedeliveryTemplateView {
    return ScheduledRedeliveryTemplateView(
        codeText = item.code,
        name = truncate(item.name, 40),
        contactEmail = truncate(item.contactEmail, 120),
        active = "${item.active}",
        source = truncate(item.source, 120),
        costText = "%.2f".format(item.cost),
        attempts = item.attempts.toString(),
        reviewer = item.reviewer,
        category = truncate(item.category, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledRedeliveryTemplateViews(items: MutableList<ScheduledRedeliveryTemplate>): MutableList<ScheduledRedeliveryTemplateView> {
    val views = mutableListOf<ScheduledRedeliveryTemplateView>()
    for (item in items) {
        views.add(toScheduledRedeliveryTemplateView(item))
    }
    return views
}

fun sumScheduledRedeliveryTemplateView(view: ScheduledRedeliveryTemplateView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedAttempts = view.attempts.toInt()
    return parsedCost + parsedAttempts.toDouble()
}
