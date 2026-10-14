package com.acme.transport.linehaul.laneplan.budget

/** How screens and exports show a lane plan notice. */
data class LanePlanNoticeView(
    var code: String,
    var name: String,
    var approved: String,
    var phoneText: String,
    var etaMinutes: String,
    var titleText: String,
    var driverIdText: String,
    var statusLabel: String,
)

fun toLanePlanNoticeView(item: LanePlanNotice): LanePlanNoticeView {
    return LanePlanNoticeView(
        code = item.code,
        name = item.name,
        approved = "${item.approved}",
        phoneText = item.phone,
        etaMinutes = item.etaMinutes.toString(),
        titleText = item.title,
        driverIdText = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanNoticeViews(items: MutableList<LanePlanNotice>): MutableList<LanePlanNoticeView> {
    val views = mutableListOf<LanePlanNoticeView>()
    for (item in items) {
        views.add(toLanePlanNoticeView(item))
    }
    return views
}

fun sumLanePlanNoticeView(view: LanePlanNoticeView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
