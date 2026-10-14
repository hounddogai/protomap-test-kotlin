package com.acme.transport.linehaul.laneplan.window

/** How screens and exports show a fallback lane plan exception. */
data class FallbackLanePlanExceptionView(
    var codeText: String,
    var label: String,
    var source: String,
    var score: String,
    var loadKgText: String,
    var visible: String,
    var etaMinutes: String,
    var palletsText: String,
    var approved: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toFallbackLanePlanExceptionView(item: FallbackLanePlanException): FallbackLanePlanExceptionView {
    return FallbackLanePlanExceptionView(
        codeText = item.code,
        label = item.label,
        source = item.source,
        score = "%.2f".format(item.score),
        loadKgText = "%.2f".format(item.loadKg),
        visible = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        palletsText = item.pallets.toString(),
        approved = "${item.approved}",
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackLanePlanExceptionViews(items: MutableList<FallbackLanePlanException>): MutableList<FallbackLanePlanExceptionView> {
    val views = mutableListOf<FallbackLanePlanExceptionView>()
    for (item in items) {
        views.add(toFallbackLanePlanExceptionView(item))
    }
    return views
}

fun sumFallbackLanePlanExceptionView(view: FallbackLanePlanExceptionView): Double {
    val parsedScore = view.score.toDouble()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedScore + parsedLoadKg + parsedEtaMinutes.toDouble()
}
