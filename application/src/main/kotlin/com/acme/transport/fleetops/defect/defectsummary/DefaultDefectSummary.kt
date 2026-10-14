package com.acme.transport.fleetops.defect.defectsummary

import com.acme.transport.kit.text.normalize

// The default defect summary records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a default defect summary is in its life cycle. */
enum class DefaultDefectSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default defect summary and its current status. */
data class DefaultDefectSummary(
    var code: String,
    var label: String,
    var rate: Double,
    var pinned: Boolean,
    var phone: String,
    var title: String,
    var status: DefaultDefectSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDefectSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDefectSummaryStatus): Boolean {
        when (status) {
            DefaultDefectSummaryStatus.OPEN -> {
                when (next) {
                    DefaultDefectSummaryStatus.RESOLVED, DefaultDefectSummaryStatus.REOPENED -> return true
                }
                return false
            }
            DefaultDefectSummaryStatus.IN_REVIEW -> {
                when (next) {
                    DefaultDefectSummaryStatus.REOPENED -> return true
                }
                return false
            }
            DefaultDefectSummaryStatus.RESOLVED -> return next == DefaultDefectSummaryStatus.ARCHIVED
            DefaultDefectSummaryStatus.REOPENED -> {
                when (next) {
                    DefaultDefectSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDefectSummaryStatus) {
        if (!canMoveTo(next)) {
            error("default defect summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default defect summary"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): DefaultDefectSummary {
        return DefaultDefectSummary(
            code = code,
            label = label,
            rate = rate,
            pinned = pinned,
            phone = phone,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default defect summary in status open. */
fun newDefaultDefectSummary(code: String, label: String): DefaultDefectSummary {
    return DefaultDefectSummary(
        code = code,
        label = label,
        rate = 0.0,
        pinned = false,
        phone = "",
        title = "",
        status = DefaultDefectSummaryStatus.OPEN,
    )
}
