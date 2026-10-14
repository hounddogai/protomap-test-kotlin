package com.acme.transport.customs.broker.allocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a broker assignment. */
data class BrokerAssignmentView(
    var code: String,
    var title: String,
    var scoreText: String,
    var endsAt: String,
    var contactEmail: String,
    var unit: String,
    var statusLabel: String,
)

fun toBrokerAssignmentView(item: BrokerAssignment): BrokerAssignmentView {
    return BrokerAssignmentView(
        code = truncate(item.code, 64),
        title = item.title,
        scoreText = "%.2f".format(item.score),
        endsAt = item.endsAt.toString(),
        contactEmail = item.contactEmail,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toBrokerAssignmentViews(items: MutableList<BrokerAssignment>): MutableList<BrokerAssignmentView> {
    val views = mutableListOf<BrokerAssignmentView>()
    for (item in items) {
        views.add(toBrokerAssignmentView(item))
    }
    return views
}

fun sumBrokerAssignmentView(view: BrokerAssignmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedScore + parsedEndsAt.toDouble()
}
