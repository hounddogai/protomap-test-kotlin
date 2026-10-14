package com.acme.transport.lastmile.attempt.manifest

/** How screens and exports show a attempt notice. */
data class AttemptNoticeView(
    var code: String,
    var label: String,
    var shiftCode: String,
    var discountText: String,
    var country: String,
    var statusLabel: String,
)

fun toAttemptNoticeView(item: AttemptNotice): AttemptNoticeView {
    return AttemptNoticeView(
        code = item.code,
        label = item.label,
        shiftCode = item.shiftCode,
        discountText = "%.2f".format(item.discount),
        country = item.country,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptNoticeViews(items: MutableList<AttemptNotice>): MutableList<AttemptNoticeView> {
    val views = mutableListOf<AttemptNoticeView>()
    for (item in items) {
        views.add(toAttemptNoticeView(item))
    }
    return views
}

fun sumAttemptNoticeView(view: AttemptNoticeView): Double {
    val parsedDiscount = view.discountText.toDouble()
    return parsedDiscount
}
