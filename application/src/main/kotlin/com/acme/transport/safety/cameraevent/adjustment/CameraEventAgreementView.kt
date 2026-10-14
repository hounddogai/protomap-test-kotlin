package com.acme.transport.safety.cameraevent.adjustment

/** How screens and exports show a camera event agreement. */
data class CameraEventAgreementView(
    var code: String,
    var label: String,
    var amount: String,
    var endsAt: String,
    var routeCodeText: String,
    var ceilingText: String,
    var statusLabel: String,
)

fun toCameraEventAgreementView(item: CameraEventAgreement): CameraEventAgreementView {
    return CameraEventAgreementView(
        code = item.code,
        label = item.label,
        amount = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        routeCodeText = item.routeCode,
        ceilingText = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCameraEventAgreementViews(items: MutableList<CameraEventAgreement>): MutableList<CameraEventAgreementView> {
    val views = mutableListOf<CameraEventAgreementView>()
    for (item in items) {
        views.add(toCameraEventAgreementView(item))
    }
    return views
}

fun sumCameraEventAgreementView(view: CameraEventAgreementView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedAmount + parsedEndsAt.toDouble() + parsedCeiling
}
