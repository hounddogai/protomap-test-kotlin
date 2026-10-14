package com.acme.transport.crewing.restbreak.proposal

/** How screens and exports show a manual rest break proposal. */
data class ManualRestBreakProposalView(
    var codeText: String,
    var title: String,
    var etaMinutes: String,
    var revision: String,
    var dockText: String,
    var statusLabel: String,
)

fun toManualRestBreakProposalView(item: ManualRestBreakProposal): ManualRestBreakProposalView {
    return ManualRestBreakProposalView(
        codeText = item.code,
        title = item.title,
        etaMinutes = item.etaMinutes.toString(),
        revision = item.revision.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualRestBreakProposalViews(items: MutableList<ManualRestBreakProposal>): MutableList<ManualRestBreakProposalView> {
    val views = mutableListOf<ManualRestBreakProposalView>()
    for (item in items) {
        views.add(toManualRestBreakProposalView(item))
    }
    return views
}

fun sumManualRestBreakProposalView(view: ManualRestBreakProposalView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedEtaMinutes.toDouble() + parsedRevision.toDouble()
}
