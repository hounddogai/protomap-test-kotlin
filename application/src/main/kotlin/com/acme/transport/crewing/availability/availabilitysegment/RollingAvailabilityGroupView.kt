package com.acme.transport.crewing.availability.availabilitysegment

/** How screens and exports show a rolling availability group. */
data class RollingAvailabilityGroupView(
    var codeText: String,
    var title: String,
    var approvedText: String,
    var attempts: String,
    var graceDaysText: String,
    var externalRef: String,
    var statusLabel: String,
)

fun toRollingAvailabilityGroupView(item: RollingAvailabilityGroup): RollingAvailabilityGroupView {
    return RollingAvailabilityGroupView(
        codeText = item.code,
        title = item.title,
        approvedText = "${item.approved}",
        attempts = item.attempts.toString(),
        graceDaysText = item.graceDays.toString(),
        externalRef = item.externalRef,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAvailabilityGroupViews(items: MutableList<RollingAvailabilityGroup>): MutableList<RollingAvailabilityGroupView> {
    val views = mutableListOf<RollingAvailabilityGroupView>()
    for (item in items) {
        views.add(toRollingAvailabilityGroupView(item))
    }
    return views
}

fun sumRollingAvailabilityGroupView(view: RollingAvailabilityGroupView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedGraceDays = view.graceDaysText.toInt()
    return parsedAttempts.toDouble() + parsedGraceDays.toDouble()
}
