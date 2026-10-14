package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled lane plan quota. */
data class ScheduledLanePlanQuotaView(
    var code: String,
    var labelText: String,
    var description: String,
    var contactEmailText: String,
    var revision: String,
    var region: String,
    var nameText: String,
    var depot: String,
    var statusLabel: String,
)

fun toScheduledLanePlanQuotaView(item: ScheduledLanePlanQuota): ScheduledLanePlanQuotaView {
    return ScheduledLanePlanQuotaView(
        code = item.code,
        labelText = truncate(item.label, 64),
        description = item.description,
        contactEmailText = item.contactEmail,
        revision = item.revision.toString(),
        region = item.region,
        nameText = item.name,
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLanePlanQuotaViews(items: MutableList<ScheduledLanePlanQuota>): MutableList<ScheduledLanePlanQuotaView> {
    val views = mutableListOf<ScheduledLanePlanQuotaView>()
    for (item in items) {
        views.add(toScheduledLanePlanQuotaView(item))
    }
    return views
}

fun sumScheduledLanePlanQuotaView(view: ScheduledLanePlanQuotaView): Double {
    val parsedRevision = view.revision.toInt()
    return parsedRevision.toDouble()
}
