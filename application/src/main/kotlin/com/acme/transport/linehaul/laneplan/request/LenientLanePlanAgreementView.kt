package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient lane plan agreement. */
data class LenientLanePlanAgreementView(
    var code: String,
    var title: String,
    var visible: String,
    var floorValueText: String,
    var dock: String,
    var vehicleIdText: String,
    var statusLabel: String,
)

fun toLenientLanePlanAgreementView(item: LenientLanePlanAgreement): LenientLanePlanAgreementView {
    return LenientLanePlanAgreementView(
        code = truncate(item.code, 64),
        title = truncate(item.title, 120),
        visible = "${item.visible}",
        floorValueText = "%.2f".format(item.floorValue),
        dock = item.dock,
        vehicleIdText = truncate(item.vehicleId, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientLanePlanAgreementViews(items: MutableList<LenientLanePlanAgreement>): MutableList<LenientLanePlanAgreementView> {
    val views = mutableListOf<LenientLanePlanAgreementView>()
    for (item in items) {
        views.add(toLenientLanePlanAgreementView(item))
    }
    return views
}

fun sumLenientLanePlanAgreementView(view: LenientLanePlanAgreementView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    return parsedFloorValue
}
