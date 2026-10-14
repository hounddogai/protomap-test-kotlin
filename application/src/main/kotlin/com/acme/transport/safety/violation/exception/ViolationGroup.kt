package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation group is in its life cycle. */
enum class ViolationGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A violation group as the violation team keeps it. */
data class ViolationGroup(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var odometer: Int,
    var maximum: Double,
    var status: ViolationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationGroupStatus): Boolean {
        when (status) {
            ViolationGroupStatus.PROPOSED -> {
                when (next) {
                    ViolationGroupStatus.APPROVED, ViolationGroupStatus.REJECTED -> return true
                }
                return false
            }
            ViolationGroupStatus.APPROVED -> {
                when (next) {
                    ViolationGroupStatus.REJECTED, ViolationGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            ViolationGroupStatus.REJECTED -> return next == ViolationGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ViolationGroupStatus) {
        if (!canMoveTo(next)) {
            error("violation group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((durationMinutes.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): ViolationGroup {
        return ViolationGroup(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            odometer = odometer,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a violation group in status proposed. */
fun newViolationGroup(code: String, title: String): ViolationGroup {
    return ViolationGroup(
        code = code,
        title = title,
        durationMinutes = 0,
        endsAt = 0,
        odometer = 0,
        maximum = 0.0,
        status = ViolationGroupStatus.PROPOSED,
    )
}
