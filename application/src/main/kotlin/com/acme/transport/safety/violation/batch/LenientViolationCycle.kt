package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient violation cycle is in its life cycle. */
enum class LenientViolationCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient violation cycle. Its code is unique within a tenant. */
data class LenientViolationCycle(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var expiresAt: Int,
    var odometer: Int,
    var stopCount: Int,
    var active: Boolean,
    var notes: String,
    var status: LenientViolationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientViolationCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientViolationCycleStatus): Boolean {
        when (status) {
            LenientViolationCycleStatus.DRAFT -> {
                when (next) {
                    LenientViolationCycleStatus.PENDING, LenientViolationCycleStatus.CLOSED -> return true
                }
                return false
            }
            LenientViolationCycleStatus.PENDING -> {
                when (next) {
                    LenientViolationCycleStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientViolationCycleStatus.ACTIVE -> return next == LenientViolationCycleStatus.SUSPENDED || next == LenientViolationCycleStatus.CLOSED
            LenientViolationCycleStatus.SUSPENDED -> return next == LenientViolationCycleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientViolationCycleStatus) {
        if (!canMoveTo(next)) {
            error("lenient violation cycle $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((odometer.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): LenientViolationCycle {
        return LenientViolationCycle(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            odometer = odometer,
            stopCount = stopCount,
            active = active,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient violation cycle in status draft. */
fun newLenientViolationCycle(code: String, label: String): LenientViolationCycle {
    return LenientViolationCycle(
        code = code,
        label = label,
        etaMinutes = 0,
        expiresAt = 0,
        odometer = 0,
        stopCount = 0,
        active = false,
        notes = "",
        status = LenientViolationCycleStatus.DRAFT,
    )
}
