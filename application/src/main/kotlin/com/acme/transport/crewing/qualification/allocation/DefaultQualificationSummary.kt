package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.text.normalize

/** Where a default qualification summary is in its life cycle. */
enum class DefaultQualificationSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One default qualification summary, with what the planners need to act on it. */
data class DefaultQualificationSummary(
    var code: String,
    var label: String,
    var graceDays: Int,
    var active: Boolean,
    var routeCode: String,
    var lane: String,
    var status: DefaultQualificationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultQualificationSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultQualificationSummaryStatus): Boolean {
        when (status) {
            DefaultQualificationSummaryStatus.DRAFT -> {
                when (next) {
                    DefaultQualificationSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultQualificationSummaryStatus.PENDING -> return next == DefaultQualificationSummaryStatus.ACTIVE || next == DefaultQualificationSummaryStatus.SUSPENDED
            DefaultQualificationSummaryStatus.ACTIVE -> return next == DefaultQualificationSummaryStatus.CLOSED
            DefaultQualificationSummaryStatus.SUSPENDED -> {
                when (next) {
                    DefaultQualificationSummaryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultQualificationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("default qualification summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): DefaultQualificationSummary {
        return DefaultQualificationSummary(
            code = code,
            label = label,
            graceDays = graceDays,
            active = active,
            routeCode = routeCode,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default qualification summary in status draft. */
fun newDefaultQualificationSummary(code: String, label: String): DefaultQualificationSummary {
    return DefaultQualificationSummary(
        code = code,
        label = label,
        graceDays = 0,
        active = false,
        routeCode = "",
        lane = "",
        status = DefaultQualificationSummaryStatus.DRAFT,
    )
}
