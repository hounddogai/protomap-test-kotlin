package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient duty period proposal. */
data class LenientDutyPeriodProposalView(
    var codeText: String,
    var name: String,
    var country: String,
    var endsAtText: String,
    var categoryText: String,
    var retries: String,
    var statusLabel: String,
)

fun toLenientDutyPeriodProposalView(item: LenientDutyPeriodProposal): LenientDutyPeriodProposalView {
    return LenientDutyPeriodProposalView(
        codeText = truncate(item.code, 40),
        name = item.name,
        country = truncate(item.country, 40),
        endsAtText = item.endsAt.toString(),
        categoryText = truncate(item.category, 80),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDutyPeriodProposalViews(items: MutableList<LenientDutyPeriodProposal>): MutableList<LenientDutyPeriodProposalView> {
    val views = mutableListOf<LenientDutyPeriodProposalView>()
    for (item in items) {
        views.add(toLenientDutyPeriodProposalView(item))
    }
    return views
}

fun sumLenientDutyPeriodProposalView(view: LenientDutyPeriodProposalView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedRetries = view.retries.toInt()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble()
}
