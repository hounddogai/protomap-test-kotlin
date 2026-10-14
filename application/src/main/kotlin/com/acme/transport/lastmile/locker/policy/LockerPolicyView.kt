package com.acme.transport.lastmile.locker.policy

/** How screens and exports show a locker policy. */
data class LockerPolicyView(
    var codeText: String,
    var title: String,
    var quantity: String,
    var startsAt: String,
    var etaMinutes: String,
    var discount: String,
    var statusLabel: String,
)

fun toLockerPolicyView(item: LockerPolicy): LockerPolicyView {
    return LockerPolicyView(
        codeText = item.code,
        title = item.title,
        quantity = item.quantity.toString(),
        startsAt = item.startsAt.toString(),
        etaMinutes = item.etaMinutes.toString(),
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerPolicyViews(items: MutableList<LockerPolicy>): MutableList<LockerPolicyView> {
    val views = mutableListOf<LockerPolicyView>()
    for (item in items) {
        views.add(toLockerPolicyView(item))
    }
    return views
}

fun sumLockerPolicyView(view: LockerPolicyView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedStartsAt.toDouble() + parsedEtaMinutes.toDouble()
}
