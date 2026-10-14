package com.acme.transport.lastmile.proofofdelivery.notice

/** How screens and exports show a adaptive proof of delivery record. */
data class AdaptiveProofOfDeliveryRecordView(
    var code: String,
    var nameText: String,
    var timezone: String,
    var fuelLiters: String,
    var phone: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toAdaptiveProofOfDeliveryRecordView(item: AdaptiveProofOfDeliveryRecord): AdaptiveProofOfDeliveryRecordView {
    return AdaptiveProofOfDeliveryRecordView(
        code = item.code,
        nameText = item.name,
        timezone = item.timezone,
        fuelLiters = "%.2f".format(item.fuelLiters),
        phone = item.phone,
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveProofOfDeliveryRecordViews(items: MutableList<AdaptiveProofOfDeliveryRecord>): MutableList<AdaptiveProofOfDeliveryRecordView> {
    val views = mutableListOf<AdaptiveProofOfDeliveryRecordView>()
    for (item in items) {
        views.add(toAdaptiveProofOfDeliveryRecordView(item))
    }
    return views
}

fun sumAdaptiveProofOfDeliveryRecordView(view: AdaptiveProofOfDeliveryRecordView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedRevision = view.revisionText.toInt()
    return parsedFuelLiters + parsedRevision.toDouble()
}
