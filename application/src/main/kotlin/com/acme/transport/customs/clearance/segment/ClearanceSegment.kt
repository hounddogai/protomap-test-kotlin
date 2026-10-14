package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance segment records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance segment is in its life cycle. */
enum class ClearanceSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One clearance segment, with what the planners need to act on it. */
data class ClearanceSegment(
    var code: String,
    var title: String,
    var graceDays: Int,
    var loadKg: Double,
    var margin: Double,
    var ceiling: Double,
    var pallets: Int,
    var status: ClearanceSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceSegmentStatus): Boolean {
        when (status) {
            ClearanceSegmentStatus.CREATED -> {
                when (next) {
                    ClearanceSegmentStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceSegmentStatus.VALIDATED -> {
                when (next) {
                    ClearanceSegmentStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceSegmentStatus.PUBLISHED -> return next == ClearanceSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ClearanceSegmentStatus) {
        if (!canMoveTo(next)) {
            error("clearance segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(pallets.toDouble() * loadKg * 100) / 100

    fun snapshot(): ClearanceSegment {
        return ClearanceSegment(
            code = code,
            title = title,
            graceDays = graceDays,
            loadKg = loadKg,
            margin = margin,
            ceiling = ceiling,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance segment in status created. */
fun newClearanceSegment(code: String, title: String): ClearanceSegment {
    return ClearanceSegment(
        code = code,
        title = title,
        graceDays = 0,
        loadKg = 0.0,
        margin = 0.0,
        ceiling = 0.0,
        pallets = 0,
        status = ClearanceSegmentStatus.CREATED,
    )
}
