package com.acme.transport.fleetops.leasereturn.preference

/** How screens and exports show a lease return agreement. */
data class LeaseReturnAgreementView(
    var code: String,
    var title: String,
    var routeCode: String,
    var shiftCode: String,
    var approved: String,
    var attempts: String,
    var statusLabel: String,
)

fun toLeaseReturnAgreementView(item: LeaseReturnAgreement): LeaseReturnAgreementView {
    return LeaseReturnAgreementView(
        code = item.code,
        title = item.title,
        routeCode = item.routeCode,
        shiftCode = item.shiftCode,
        approved = "${item.approved}",
        attempts = item.attempts.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLeaseReturnAgreementViews(items: MutableList<LeaseReturnAgreement>): MutableList<LeaseReturnAgreementView> {
    val views = mutableListOf<LeaseReturnAgreementView>()
    for (item in items) {
        views.add(toLeaseReturnAgreementView(item))
    }
    return views
}

fun sumLeaseReturnAgreementView(view: LeaseReturnAgreementView): Double {
    val parsedAttempts = view.attempts.toInt()
    return parsedAttempts.toDouble()
}
