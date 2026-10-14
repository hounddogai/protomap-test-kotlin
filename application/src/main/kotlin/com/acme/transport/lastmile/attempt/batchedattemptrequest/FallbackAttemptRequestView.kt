package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback attempt request. */
data class FallbackAttemptRequestView(
    var codeText: String,
    var labelText: String,
    var description: String,
    var phone: String,
    var createdAt: String,
    var segment: String,
    var batchSize: String,
    var externalRef: String,
    var ownerId: String,
    var statusLabel: String,
)

fun toFallbackAttemptRequestView(item: FallbackAttemptRequest): FallbackAttemptRequestView {
    return FallbackAttemptRequestView(
        codeText = truncate(item.code, 120),
        labelText = item.label,
        description = item.description,
        phone = item.phone,
        createdAt = item.createdAt.toString(),
        segment = item.segment,
        batchSize = item.batchSize.toString(),
        externalRef = item.externalRef,
        ownerId = item.ownerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAttemptRequestViews(items: MutableList<FallbackAttemptRequest>): MutableList<FallbackAttemptRequestView> {
    val views = mutableListOf<FallbackAttemptRequestView>()
    for (item in items) {
        views.add(toFallbackAttemptRequestView(item))
    }
    return views
}

fun sumFallbackAttemptRequestView(view: FallbackAttemptRequestView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedCreatedAt.toDouble() + parsedBatchSize.toDouble()
}
