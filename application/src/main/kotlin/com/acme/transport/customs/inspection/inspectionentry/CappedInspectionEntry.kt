package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped inspection entry records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a capped inspection entry is in its life cycle. */
enum class CappedInspectionEntryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capped inspection entry, with what the planners need to act on it. */
data class CappedInspectionEntry(
    var code: String,
    var title: String,
    var odometer: Int,
    var category: String,
    var threshold: Double,
    var amount: Double,
    var carrier: String,
    var source: String,
    var version: Int,
    var sequence: Int,
    var status: CappedInspectionEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionEntryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionEntryStatus): Boolean {
        when (status) {
            CappedInspectionEntryStatus.PROPOSED -> return next == CappedInspectionEntryStatus.APPROVED || next == CappedInspectionEntryStatus.REJECTED
            CappedInspectionEntryStatus.APPROVED -> {
                when (next) {
                    CappedInspectionEntryStatus.REJECTED, CappedInspectionEntryStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedInspectionEntryStatus.REJECTED -> return next == CappedInspectionEntryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedInspectionEntryStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection entry $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"capped inspection entry"} $code"

    fun projectedValue(): Double = round((odometer.toDouble() - threshold) * 100) / 100

    fun snapshot(): CappedInspectionEntry {
        return CappedInspectionEntry(
            code = code,
            title = title,
            odometer = odometer,
            category = category,
            threshold = threshold,
            amount = amount,
            carrier = carrier,
            source = source,
            version = version,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped inspection entry in status proposed. */
fun newCappedInspectionEntry(code: String, title: String): CappedInspectionEntry {
    return CappedInspectionEntry(
        code = code,
        title = title,
        odometer = 0,
        category = "",
        threshold = 0.0,
        amount = 0.0,
        carrier = "",
        source = "",
        version = 0,
        sequence = 0,
        status = CappedInspectionEntryStatus.PROPOSED,
    )
}
