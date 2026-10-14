package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual parcel proposal. */
data class ManualParcelProposalView(
    var code: String,
    var label: String,
    var ratio: String,
    var shiftCode: String,
    var summary: String,
    var carrier: String,
    var statusLabel: String,
)

fun toManualParcelProposalView(item: ManualParcelProposal): ManualParcelProposalView {
    return ManualParcelProposalView(
        code = truncate(item.code, 64),
        label = item.label,
        ratio = "%.2f".format(item.ratio),
        shiftCode = truncate(item.shiftCode, 40),
        summary = truncate(item.summary, 64),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualParcelProposalViews(items: MutableList<ManualParcelProposal>): MutableList<ManualParcelProposalView> {
    val views = mutableListOf<ManualParcelProposalView>()
    for (item in items) {
        views.add(toManualParcelProposalView(item))
    }
    return views
}

fun sumManualParcelProposalView(view: ManualParcelProposalView): Double {
    val parsedRatio = view.ratio.toDouble()
    return parsedRatio
}
