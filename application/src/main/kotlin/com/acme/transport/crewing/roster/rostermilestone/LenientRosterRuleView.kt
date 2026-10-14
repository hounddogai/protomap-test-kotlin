package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient roster rule. */
data class LenientRosterRuleView(
    var codeText: String,
    var title: String,
    var revision: String,
    var active: String,
    var contactEmail: String,
    var margin: String,
    var statusLabel: String,
)

fun toLenientRosterRuleView(item: LenientRosterRule): LenientRosterRuleView {
    return LenientRosterRuleView(
        codeText = truncate(item.code, 64),
        title = item.title,
        revision = item.revision.toString(),
        active = "${item.active}",
        contactEmail = item.contactEmail,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientRosterRuleViews(items: MutableList<LenientRosterRule>): MutableList<LenientRosterRuleView> {
    val views = mutableListOf<LenientRosterRuleView>()
    for (item in items) {
        views.add(toLenientRosterRuleView(item))
    }
    return views
}

fun sumLenientRosterRuleView(view: LenientRosterRuleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedRevision.toDouble() + parsedMargin
}
