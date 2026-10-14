package com.acme.transport.lastmile.drop.recorditem

/** How screens and exports show a global drop exception. */
data class GlobalDropExceptionView(
    var codeText: String,
    var nameText: String,
    var sequence: String,
    var createdAt: String,
    var limitText: String,
    var expiresAt: String,
    var lane: String,
    var statusLabel: String,
)

fun toGlobalDropExceptionView(item: GlobalDropException): GlobalDropExceptionView {
    return GlobalDropExceptionView(
        codeText = item.code,
        nameText = item.name,
        sequence = item.sequence.toString(),
        createdAt = item.createdAt.toString(),
        limitText = item.limit.toString(),
        expiresAt = item.expiresAt.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDropExceptionViews(items: MutableList<GlobalDropException>): MutableList<GlobalDropExceptionView> {
    val views = mutableListOf<GlobalDropExceptionView>()
    for (item in items) {
        views.add(toGlobalDropExceptionView(item))
    }
    return views
}

fun sumGlobalDropExceptionView(view: GlobalDropExceptionView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedLimit = view.limitText.toInt()
    return parsedSequence.toDouble() + parsedCreatedAt.toDouble() + parsedLimit.toDouble()
}
