package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a declaration notice. */
data class DeclarationNoticeView(
    var code: String,
    var titleText: String,
    var country: String,
    var windowDays: String,
    var amountText: String,
    var notesText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toDeclarationNoticeView(item: DeclarationNotice): DeclarationNoticeView {
    return DeclarationNoticeView(
        code = item.code,
        titleText = truncate(item.title, 80),
        country = truncate(item.country, 40),
        windowDays = item.windowDays.toString(),
        amountText = "%.2f".format(item.amount),
        notesText = item.notes,
        carrier = truncate(item.carrier, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDeclarationNoticeViews(items: MutableList<DeclarationNotice>): MutableList<DeclarationNoticeView> {
    val views = mutableListOf<DeclarationNoticeView>()
    for (item in items) {
        views.add(toDeclarationNoticeView(item))
    }
    return views
}

fun sumDeclarationNoticeView(view: DeclarationNoticeView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedAmount = view.amountText.toDouble()
    return parsedWindowDays.toDouble() + parsedAmount
}
