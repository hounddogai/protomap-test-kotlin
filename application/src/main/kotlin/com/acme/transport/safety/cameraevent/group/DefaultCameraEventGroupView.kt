package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default camera event group. */
data class DefaultCameraEventGroupView(
    var code: String,
    var label: String,
    var depot: String,
    var discount: String,
    var pinned: String,
    var statusLabel: String,
)

fun toDefaultCameraEventGroupView(item: DefaultCameraEventGroup): DefaultCameraEventGroupView {
    return DefaultCameraEventGroupView(
        code = truncate(item.code, 64),
        label = item.label,
        depot = item.depot,
        discount = "%.2f".format(item.discount),
        pinned = "${item.pinned}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCameraEventGroupViews(items: MutableList<DefaultCameraEventGroup>): MutableList<DefaultCameraEventGroupView> {
    val views = mutableListOf<DefaultCameraEventGroupView>()
    for (item in items) {
        views.add(toDefaultCameraEventGroupView(item))
    }
    return views
}

fun sumDefaultCameraEventGroupView(view: DefaultCameraEventGroupView): Double {
    val parsedDiscount = view.discount.toDouble()
    return parsedDiscount
}
