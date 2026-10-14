package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation target is in its life cycle. */
enum class ViolationTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One violation target, with what the planners need to act on it. */
data class ViolationTarget(
    var code: String,
    var label: String,
    var shiftCode: String,
    var etaMinutes: Int,
    var capacity: Int,
    var status: ViolationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationTargetStatus): Boolean {
        when (status) {
            ViolationTargetStatus.REQUESTED -> return next == ViolationTargetStatus.GRANTED || next == ViolationTargetStatus.REVOKED
            ViolationTargetStatus.GRANTED -> return next == ViolationTargetStatus.EXPIRED
            ViolationTargetStatus.REVOKED -> return next == ViolationTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ViolationTargetStatus) {
        if (!canMoveTo(next)) {
            error("violation target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((etaMinutes.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): ViolationTarget {
        return ViolationTarget(
            code = code,
            label = label,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation target in status requested. */
fun newViolationTarget(code: String, label: String): ViolationTarget {
    return ViolationTarget(
        code = code,
        label = label,
        shiftCode = "",
        etaMinutes = 0,
        capacity = 0,
        status = ViolationTargetStatus.REQUESTED,
    )
}
