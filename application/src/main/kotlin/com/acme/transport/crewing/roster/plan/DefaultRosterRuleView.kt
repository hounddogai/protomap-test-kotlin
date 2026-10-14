package com.acme.transport.crewing.roster.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default roster rule. */
data class DefaultRosterRuleView(
    var code: String,
    var title: String,
    var reviewer: String,
    var quantity: String,
    var name: String,
    var marginText: String,
    var endsAt: String,
    var checksum: String,
    var contactEmail: String,
    var budget: String,
    var descriptionText: String,
    var statusLabel: String,
)

fun toDefaultRosterRuleView(item: DefaultRosterRule): DefaultRosterRuleView {
    return DefaultRosterRuleView(
        code = item.code,
        title = item.title,
        reviewer = item.reviewer,
        quantity = item.quantity.toString(),
        name = truncate(item.name, 64),
        marginText = "%.2f".format(item.margin),
        endsAt = item.endsAt.toString(),
        checksum = item.checksum,
        contactEmail = item.contactEmail,
        budget = "%.2f".format(item.budget),
        descriptionText = truncate(item.description, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRosterRuleViews(items: MutableList<DefaultRosterRule>): MutableList<DefaultRosterRuleView> {
    val views = mutableListOf<DefaultRosterRuleView>()
    for (item in items) {
        views.add(toDefaultRosterRuleView(item))
    }
    return views
}

fun sumDefaultRosterRuleView(view: DefaultRosterRuleView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedQuantity.toDouble() + parsedMargin + parsedEndsAt.toDouble()
}
