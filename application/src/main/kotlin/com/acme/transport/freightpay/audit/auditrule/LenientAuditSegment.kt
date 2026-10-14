package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient audit segment is in its life cycle. */
enum class LenientAuditSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lenient audit segment, with what the planners need to act on it. */
data class LenientAuditSegment(
    var code: String,
    var label: String,
    var maximum: Double,
    var updatedAt: Int,
    var timezone: String,
    var weight: Double,
    var rate: Double,
    var status: LenientAuditSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAuditSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAuditSegmentStatus): Boolean {
        when (status) {
            LenientAuditSegmentStatus.NEW -> return next == LenientAuditSegmentStatus.QUEUED || next == LenientAuditSegmentStatus.DONE
            LenientAuditSegmentStatus.QUEUED -> return next == LenientAuditSegmentStatus.RUNNING || next == LenientAuditSegmentStatus.FAILED
            LenientAuditSegmentStatus.RUNNING -> {
                when (next) {
                    LenientAuditSegmentStatus.DONE, LenientAuditSegmentStatus.FAILED -> return true
                }
                return false
            }
            LenientAuditSegmentStatus.DONE -> {
                when (next) {
                    LenientAuditSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAuditSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient audit segment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round(updatedAt.toDouble() * rate * 100) / 100

    fun snapshot(): LenientAuditSegment {
        return LenientAuditSegment(
            code = code,
            label = label,
            maximum = maximum,
            updatedAt = updatedAt,
            timezone = timezone,
            weight = weight,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient audit segment in status new. */
fun newLenientAuditSegment(code: String, label: String): LenientAuditSegment {
    return LenientAuditSegment(
        code = code,
        label = label,
        maximum = 0.0,
        updatedAt = 0,
        timezone = "",
        weight = 0.0,
        rate = 0.0,
        status = LenientAuditSegmentStatus.NEW,
    )
}
