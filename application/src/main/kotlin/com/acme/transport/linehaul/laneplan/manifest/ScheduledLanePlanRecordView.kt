package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lane plan record. */
data class ScheduledLanePlanRecordView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var createdAtText: String,
    var region: String,
    var rate: String,
    var currencyText: String,
    var category: String,
    var batchSize: String,
    var description: String,
    var title: String,
    var statusLabel: String,
)

fun toScheduledLanePlanRecordView(item: ScheduledLanePlanRecord): ScheduledLanePlanRecordView {
    return ScheduledLanePlanRecordView(
        code = truncate(item.code, 80),
        label = item.label,
        contactEmail = truncate(item.contactEmail, 80),
        createdAtText = item.createdAt.toString(),
        region = truncate(item.region, 120),
        rate = "%.2f".format(item.rate),
        currencyText = item.currency,
        category = item.category,
        batchSize = item.batchSize.toString(),
        description = item.description,
        title = item.title,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanRecordViews(items: MutableList<ScheduledLanePlanRecord>): MutableList<ScheduledLanePlanRecordView> {
    val views = mutableListOf<ScheduledLanePlanRecordView>()
    for (item in items) {
        views.add(toScheduledLanePlanRecordView(item))
    }
    return views
}

fun sumScheduledLanePlanRecordView(view: ScheduledLanePlanRecordView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedCreatedAt.toDouble() + parsedRate + parsedBatchSize.toDouble()
}
