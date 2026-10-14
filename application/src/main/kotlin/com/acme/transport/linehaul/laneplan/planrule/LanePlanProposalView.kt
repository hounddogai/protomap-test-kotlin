package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan proposal. */
data class LanePlanProposalView(
    var code: String,
    var title: String,
    var limitText: String,
    var marginText: String,
    var sequence: String,
    var retries: String,
    var locale: String,
    var statusLabel: String,
)

fun toLanePlanProposalView(item: LanePlanProposal): LanePlanProposalView {
    return LanePlanProposalView(
        code = item.code,
        title = truncate(item.title, 80),
        limitText = item.limit.toString(),
        marginText = "%.2f".format(item.margin),
        sequence = item.sequence.toString(),
        retries = item.retries.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanProposalViews(items: MutableList<LanePlanProposal>): MutableList<LanePlanProposalView> {
    val views = mutableListOf<LanePlanProposalView>()
    for (item in items) {
        views.add(toLanePlanProposalView(item))
    }
    return views
}

fun sumLanePlanProposalView(view: LanePlanProposalView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedSequence = view.sequence.toInt()
    return parsedLimit.toDouble() + parsedMargin + parsedSequence.toDouble()
}
