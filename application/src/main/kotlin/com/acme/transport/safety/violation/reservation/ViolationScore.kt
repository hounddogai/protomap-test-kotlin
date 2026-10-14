package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation score is in its life cycle. */
enum class ViolationScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A violation score. Its code is unique within a tenant. */
data class ViolationScore(
    var code: String,
    var label: String,
    var unit: String,
    var sequence: Int,
    var odometer: Int,
    var checksum: String,
    var pinned: Boolean,
    var summary: String,
    var version: Int,
    var currency: String,
    var status: ViolationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationScoreStatus): Boolean {
        when (status) {
            ViolationScoreStatus.DRAFT -> return next == ViolationScoreStatus.PENDING || next == ViolationScoreStatus.SUSPENDED
            ViolationScoreStatus.PENDING -> {
                when (next) {
                    ViolationScoreStatus.SUSPENDED -> return true
                }
                return false
            }
            ViolationScoreStatus.ACTIVE -> return next == ViolationScoreStatus.SUSPENDED
            ViolationScoreStatus.SUSPENDED -> return next == ViolationScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ViolationScoreStatus) {
        if (!canMoveTo(next)) {
            error("violation score $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round(sequence.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ViolationScore {
        return ViolationScore(
            code = code,
            label = label,
            unit = unit,
            sequence = sequence,
            odometer = odometer,
            checksum = checksum,
            pinned = pinned,
            summary = summary,
            version = version,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a violation score in status draft. */
fun newViolationScore(code: String, label: String): ViolationScore {
    return ViolationScore(
        code = code,
        label = label,
        unit = "",
        sequence = 0,
        odometer = 0,
        checksum = "",
        pinned = false,
        summary = "",
        version = 0,
        currency = "",
        status = ViolationScoreStatus.DRAFT,
    )
}
