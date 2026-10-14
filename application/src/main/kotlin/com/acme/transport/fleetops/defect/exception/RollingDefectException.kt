package com.acme.transport.fleetops.defect.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling defect exception records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a rolling defect exception is in its life cycle. */
enum class RollingDefectExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling defect exception, with what the planners need to act on it. */
data class RollingDefectException(
    var code: String,
    var title: String,
    var segment: String,
    var carrier: String,
    var externalRef: String,
    var shiftCode: String,
    var durationMinutes: Int,
    var attempts: Int,
    var createdAt: Int,
    var score: Double,
    var status: RollingDefectExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDefectExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDefectExceptionStatus): Boolean {
        when (status) {
            RollingDefectExceptionStatus.PLANNED -> {
                when (next) {
                    RollingDefectExceptionStatus.SCHEDULED, RollingDefectExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RollingDefectExceptionStatus.SCHEDULED -> {
                when (next) {
                    RollingDefectExceptionStatus.IN_PROGRESS, RollingDefectExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            RollingDefectExceptionStatus.IN_PROGRESS -> return next == RollingDefectExceptionStatus.COMPLETED
            RollingDefectExceptionStatus.COMPLETED -> return next == RollingDefectExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingDefectExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rolling defect exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round(attempts.toDouble() * score * 100) / 100

    fun snapshot(): RollingDefectException {
        return RollingDefectException(
            code = code,
            title = title,
            segment = segment,
            carrier = carrier,
            externalRef = externalRef,
            shiftCode = shiftCode,
            durationMinutes = durationMinutes,
            attempts = attempts,
            createdAt = createdAt,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling defect exception in status planned. */
fun newRollingDefectException(code: String, title: String): RollingDefectException {
    return RollingDefectException(
        code = code,
        title = title,
        segment = "",
        carrier = "",
        externalRef = "",
        shiftCode = "",
        durationMinutes = 0,
        attempts = 0,
        createdAt = 0,
        score = 0.0,
        status = RollingDefectExceptionStatus.PLANNED,
    )
}
