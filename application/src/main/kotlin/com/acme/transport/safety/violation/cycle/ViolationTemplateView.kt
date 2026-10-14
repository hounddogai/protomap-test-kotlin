package com.acme.transport.safety.violation.cycle

/** How screens and exports show a violation template. */
data class ViolationTemplateView(
    var code: String,
    var label: String,
    var timezone: String,
    var maximum: String,
    var createdAt: String,
    var countryText: String,
    var graceDays: String,
    var cost: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toViolationTemplateView(item: ViolationTemplate): ViolationTemplateView {
    return ViolationTemplateView(
        code = item.code,
        label = item.label,
        timezone = item.timezone,
        maximum = "%.2f".format(item.maximum),
        createdAt = item.createdAt.toString(),
        countryText = item.country,
        graceDays = item.graceDays.toString(),
        cost = "%.2f".format(item.cost),
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toViolationTemplateViews(items: MutableList<ViolationTemplate>): MutableList<ViolationTemplateView> {
    val views = mutableListOf<ViolationTemplateView>()
    for (item in items) {
        views.add(toViolationTemplateView(item))
    }
    return views
}

fun sumViolationTemplateView(view: ViolationTemplateView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedMaximum + parsedCreatedAt.toDouble() + parsedGraceDays.toDouble()
}
