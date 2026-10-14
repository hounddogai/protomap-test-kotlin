package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** How screens and exports show a attempt schedule. */
data class AttemptScheduleView(
    var codeText: String,
    var name: String,
    var approved: String,
    var summary: String,
    var revision: String,
    var batchSize: String,
    var rate: String,
    var statusLabel: String,
)

fun toAttemptScheduleView(item: AttemptSchedule): AttemptScheduleView {
    return AttemptScheduleView(
        codeText = item.code,
        name = item.name,
        approved = "${item.approved}",
        summary = item.summary,
        revision = item.revision.toString(),
        batchSize = item.batchSize.toString(),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAttemptScheduleViews(items: MutableList<AttemptSchedule>): MutableList<AttemptScheduleView> {
    val views = mutableListOf<AttemptScheduleView>()
    for (item in items) {
        views.add(toAttemptScheduleView(item))
    }
    return views
}

fun sumAttemptScheduleView(view: AttemptScheduleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedRevision.toDouble() + parsedBatchSize.toDouble() + parsedRate
}
