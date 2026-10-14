package com.acme.transport.emissions.emissionfactor.entry

/** How screens and exports show a rolling emission factor quota. */
data class RollingEmissionFactorQuotaView(
    var codeText: String,
    var name: String,
    var summaryText: String,
    var attemptsText: String,
    var locale: String,
    var active: String,
    var windowDays: String,
    var statusLabel: String,
)

fun toRollingEmissionFactorQuotaView(item: RollingEmissionFactorQuota): RollingEmissionFactorQuotaView {
    return RollingEmissionFactorQuotaView(
        codeText = item.code,
        name = item.name,
        summaryText = item.summary,
        attemptsText = item.attempts.toString(),
        locale = item.locale,
        active = "${item.active}",
        windowDays = item.windowDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingEmissionFactorQuotaViews(items: MutableList<RollingEmissionFactorQuota>): MutableList<RollingEmissionFactorQuotaView> {
    val views = mutableListOf<RollingEmissionFactorQuotaView>()
    for (item in items) {
        views.add(toRollingEmissionFactorQuotaView(item))
    }
    return views
}

fun sumRollingEmissionFactorQuotaView(view: RollingEmissionFactorQuotaView): Double {
    val parsedAttempts = view.attemptsText.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedAttempts.toDouble() + parsedWindowDays.toDouble()
}
