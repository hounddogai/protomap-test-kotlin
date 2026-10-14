package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling availability group is in its life cycle. */
enum class RollingAvailabilityGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One rolling availability group, with what the planners need to act on it. */
data class RollingAvailabilityGroup(
    var code: String,
    var title: String,
    var approved: Boolean,
    var attempts: Int,
    var graceDays: Int,
    var externalRef: String,
    var status: RollingAvailabilityGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAvailabilityGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAvailabilityGroupStatus): Boolean {
        when (status) {
            RollingAvailabilityGroupStatus.DRAFT -> return next == RollingAvailabilityGroupStatus.PENDING || next == RollingAvailabilityGroupStatus.SUSPENDED
            RollingAvailabilityGroupStatus.PENDING -> {
                when (next) {
                    RollingAvailabilityGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingAvailabilityGroupStatus.ACTIVE -> return next == RollingAvailabilityGroupStatus.SUSPENDED
            RollingAvailabilityGroupStatus.SUSPENDED -> return next == RollingAvailabilityGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingAvailabilityGroupStatus) {
        if (!canMoveTo(next)) {
            error("rolling availability group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((graceDays.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): RollingAvailabilityGroup {
        return RollingAvailabilityGroup(
            code = code,
            title = title,
            approved = approved,
            attempts = attempts,
            graceDays = graceDays,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling availability group in status draft. */
fun newRollingAvailabilityGroup(code: String, title: String): RollingAvailabilityGroup {
    return RollingAvailabilityGroup(
        code = code,
        title = title,
        approved = false,
        attempts = 0,
        graceDays = 0,
        externalRef = "",
        status = RollingAvailabilityGroupStatus.DRAFT,
    )
}
