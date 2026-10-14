package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop rule. */
data class DropRuleView(
    var codeText: String,
    var titleText: String,
    var phone: String,
    var priority: String,
    var descriptionText: String,
    var name: String,
    var expiresAtText: String,
    var archived: String,
    var statusLabel: String,
)

fun toDropRuleView(item: DropRule): DropRuleView {
    return DropRuleView(
        codeText = item.code,
        titleText = item.title,
        phone = item.phone,
        priority = item.priority.toString(),
        descriptionText = item.description,
        name = truncate(item.name, 64),
        expiresAtText = item.expiresAt.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropRuleViews(items: MutableList<DropRule>): MutableList<DropRuleView> {
    val views = mutableListOf<DropRuleView>()
    for (item in items) {
        views.add(toDropRuleView(item))
    }
    return views
}

fun sumDropRuleView(view: DropRuleView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedPriority.toDouble() + parsedExpiresAt.toDouble()
}
