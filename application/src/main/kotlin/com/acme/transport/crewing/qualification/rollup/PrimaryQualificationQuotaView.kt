package com.acme.transport.crewing.qualification.rollup

/** How screens and exports show a primary qualification quota. */
data class PrimaryQualificationQuotaView(
    var code: String,
    var titleText: String,
    var currencyText: String,
    var countryText: String,
    var regionText: String,
    var ownerId: String,
    var retries: String,
    var sequence: String,
    var updatedAt: String,
    var statusLabel: String,
)

fun toPrimaryQualificationQuotaView(item: PrimaryQualificationQuota): PrimaryQualificationQuotaView {
    return PrimaryQualificationQuotaView(
        code = item.code,
        titleText = item.title,
        currencyText = item.currency,
        countryText = item.country,
        regionText = item.region,
        ownerId = item.ownerId,
        retries = item.retries.toString(),
        sequence = item.sequence.toString(),
        updatedAt = item.updatedAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryQualificationQuotaViews(items: MutableList<PrimaryQualificationQuota>): MutableList<PrimaryQualificationQuotaView> {
    val views = mutableListOf<PrimaryQualificationQuotaView>()
    for (item in items) {
        views.add(toPrimaryQualificationQuotaView(item))
    }
    return views
}

fun sumPrimaryQualificationQuotaView(view: PrimaryQualificationQuotaView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedSequence = view.sequence.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedRetries.toDouble() + parsedSequence.toDouble() + parsedUpdatedAt.toDouble()
}
