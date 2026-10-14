package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect summary records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect summary is in its life cycle. */
enum class DefectSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a defect summary and its current status. */
data class DefectSummary(
    var code: String,
    var name: String,
    var minimum: Double,
    var archived: Boolean,
    var ceiling: Double,
    var status: DefectSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectSummaryStatus): Boolean {
        when (status) {
            DefectSummaryStatus.PROPOSED -> {
                when (next) {
                    DefectSummaryStatus.APPROVED, DefectSummaryStatus.REJECTED -> return true
                }
                return false
            }
            DefectSummaryStatus.APPROVED -> return next == DefectSummaryStatus.REJECTED
            DefectSummaryStatus.REJECTED -> {
                when (next) {
                    DefectSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectSummaryStatus) {
        if (!canMoveTo(next)) {
            error("defect summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((minimum + ceiling) * 100) / 100

    fun snapshot(): DefectSummary {
        return DefectSummary(
            code = code,
            name = name,
            minimum = minimum,
            archived = archived,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect summary in status proposed. */
fun newDefectSummary(code: String, name: String): DefectSummary {
    return DefectSummary(
        code = code,
        name = name,
        minimum = 0.0,
        archived = false,
        ceiling = 0.0,
        status = DefectSummaryStatus.PROPOSED,
    )
}
