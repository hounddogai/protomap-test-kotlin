package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped inspection summary records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a capped inspection summary is in its life cycle. */
enum class CappedInspectionSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped inspection summary and its current status. */
data class CappedInspectionSummary(
    var code: String,
    var title: String,
    var discount: Double,
    var margin: Double,
    var unit: String,
    var status: CappedInspectionSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionSummaryStatus): Boolean {
        when (status) {
            CappedInspectionSummaryStatus.PROPOSED -> return next == CappedInspectionSummaryStatus.APPROVED
            CappedInspectionSummaryStatus.APPROVED -> return next == CappedInspectionSummaryStatus.REJECTED
            CappedInspectionSummaryStatus.REJECTED -> return next == CappedInspectionSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedInspectionSummaryStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((margin + discount) * 100) / 100

    fun snapshot(): CappedInspectionSummary {
        return CappedInspectionSummary(
            code = code,
            title = title,
            discount = discount,
            margin = margin,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped inspection summary in status proposed. */
fun newCappedInspectionSummary(code: String, title: String): CappedInspectionSummary {
    return CappedInspectionSummary(
        code = code,
        title = title,
        discount = 0.0,
        margin = 0.0,
        unit = "",
        status = CappedInspectionSummaryStatus.PROPOSED,
    )
}
