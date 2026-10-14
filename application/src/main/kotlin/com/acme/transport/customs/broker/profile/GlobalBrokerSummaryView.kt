package com.acme.transport.customs.broker.profile

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global broker summary. */
data class GlobalBrokerSummaryView(
    var code: String,
    var name: String,
    var priority: String,
    var sourceText: String,
    var summary: String,
    var ownerIdText: String,
    var statusLabel: String,
)

fun toGlobalBrokerSummaryView(item: GlobalBrokerSummary): GlobalBrokerSummaryView {
    return GlobalBrokerSummaryView(
        code = item.code,
        name = item.name,
        priority = item.priority.toString(),
        sourceText = truncate(item.source, 64),
        summary = truncate(item.summary, 80),
        ownerIdText = truncate(item.ownerId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalBrokerSummaryViews(items: MutableList<GlobalBrokerSummary>): MutableList<GlobalBrokerSummaryView> {
    val views = mutableListOf<GlobalBrokerSummaryView>()
    for (item in items) {
        views.add(toGlobalBrokerSummaryView(item))
    }
    return views
}

fun sumGlobalBrokerSummaryView(view: GlobalBrokerSummaryView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
