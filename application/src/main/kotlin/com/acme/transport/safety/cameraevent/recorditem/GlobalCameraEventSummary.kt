package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.text.normalize

/** Where a global camera event summary is in its life cycle. */
enum class GlobalCameraEventSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a global camera event summary and its current status. */
data class GlobalCameraEventSummary(
    var code: String,
    var label: String,
    var dock: String,
    var ownerId: String,
    var limit: Int,
    var routeCode: String,
    var status: GlobalCameraEventSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCameraEventSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCameraEventSummaryStatus): Boolean {
        when (status) {
            GlobalCameraEventSummaryStatus.OPEN -> return next == GlobalCameraEventSummaryStatus.ARCHIVED
            GlobalCameraEventSummaryStatus.IN_REVIEW -> return next == GlobalCameraEventSummaryStatus.REOPENED || next == GlobalCameraEventSummaryStatus.ARCHIVED
            GlobalCameraEventSummaryStatus.RESOLVED -> return next == GlobalCameraEventSummaryStatus.ARCHIVED
            GlobalCameraEventSummaryStatus.REOPENED -> return next == GlobalCameraEventSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalCameraEventSummaryStatus) {
        if (!canMoveTo(next)) {
            error("global camera event summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): GlobalCameraEventSummary {
        return GlobalCameraEventSummary(
            code = code,
            label = label,
            dock = dock,
            ownerId = ownerId,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global camera event summary in status open. */
fun newGlobalCameraEventSummary(code: String, label: String): GlobalCameraEventSummary {
    return GlobalCameraEventSummary(
        code = code,
        label = label,
        dock = "",
        ownerId = "",
        limit = 0,
        routeCode = "",
        status = GlobalCameraEventSummaryStatus.OPEN,
    )
}
