package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer record records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer record is in its life cycle. */
enum class TrailerRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A trailer record as the trailer team keeps it. */
data class TrailerRecord(
    var code: String,
    var label: String,
    var threshold: Double,
    var routeCode: String,
    var durationMinutes: Int,
    var status: TrailerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerRecordStatus): Boolean {
        when (status) {
            TrailerRecordStatus.PROPOSED -> {
                when (next) {
                    TrailerRecordStatus.APPROVED, TrailerRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerRecordStatus.APPROVED -> return next == TrailerRecordStatus.REJECTED || next == TrailerRecordStatus.WITHDRAWN
            TrailerRecordStatus.REJECTED -> {
                when (next) {
                    TrailerRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerRecordStatus) {
        if (!canMoveTo(next)) {
            error("trailer record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round(threshold * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): TrailerRecord {
        return TrailerRecord(
            code = code,
            label = label,
            threshold = threshold,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer record in status proposed. */
fun newTrailerRecord(code: String, label: String): TrailerRecord {
    return TrailerRecord(
        code = code,
        label = label,
        threshold = 0.0,
        routeCode = "",
        durationMinutes = 0,
        status = TrailerRecordStatus.PROPOSED,
    )
}
