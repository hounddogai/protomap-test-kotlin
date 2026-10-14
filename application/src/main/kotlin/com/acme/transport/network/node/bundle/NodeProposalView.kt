package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a node proposal. */
data class NodeProposalView(
    var codeText: String,
    var label: String,
    var description: String,
    var phone: String,
    var unitText: String,
    var source: String,
    var loadKg: String,
    var statusLabel: String,
)

fun toNodeProposalView(item: NodeProposal): NodeProposalView {
    return NodeProposalView(
        codeText = item.code,
        label = item.label,
        description = truncate(item.description, 64),
        phone = item.phone,
        unitText = item.unit,
        source = item.source,
        loadKg = "%.2f".format(item.loadKg),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toNodeProposalViews(items: MutableList<NodeProposal>): MutableList<NodeProposalView> {
    val views = mutableListOf<NodeProposalView>()
    for (item in items) {
        views.add(toNodeProposalView(item))
    }
    return views
}

fun sumNodeProposalView(view: NodeProposalView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedLoadKg
}
