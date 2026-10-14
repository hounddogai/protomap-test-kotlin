package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending lane plan draft. */
data class PendingLanePlanDraftView(
    var code: String,
    var name: String,
    var loadKg: String,
    var externalRefText: String,
    var batchSizeText: String,
    var archived: String,
    var statusLabel: String,
)

fun toPendingLanePlanDraftView(item: PendingLanePlanDraft): PendingLanePlanDraftView {
    return PendingLanePlanDraftView(
        code = item.code,
        name = item.name,
        loadKg = "%.2f".format(item.loadKg),
        externalRefText = truncate(item.externalRef, 64),
        batchSizeText = item.batchSize.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingLanePlanDraftViews(items: MutableList<PendingLanePlanDraft>): MutableList<PendingLanePlanDraftView> {
    val views = mutableListOf<PendingLanePlanDraftView>()
    for (item in items) {
        views.add(toPendingLanePlanDraftView(item))
    }
    return views
}

fun sumPendingLanePlanDraftView(view: PendingLanePlanDraftView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedLoadKg + parsedBatchSize.toDouble()
}
