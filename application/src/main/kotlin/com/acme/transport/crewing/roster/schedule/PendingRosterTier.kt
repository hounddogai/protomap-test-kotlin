package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending roster tier is in its life cycle. */
enum class PendingRosterTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One pending roster tier, with what the planners need to act on it. */
data class PendingRosterTier(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var depot: String,
    var phone: String,
    var weight: Double,
    var stopCount: Int,
    var shiftCode: String,
    var status: PendingRosterTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterTierStatus): Boolean {
        when (status) {
            PendingRosterTierStatus.OPEN -> return next == PendingRosterTierStatus.REOPENED || next == PendingRosterTierStatus.ARCHIVED
            PendingRosterTierStatus.IN_REVIEW -> return next == PendingRosterTierStatus.REOPENED || next == PendingRosterTierStatus.ARCHIVED
            PendingRosterTierStatus.RESOLVED -> {
                when (next) {
                    PendingRosterTierStatus.REOPENED, PendingRosterTierStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingRosterTierStatus.REOPENED -> {
                when (next) {
                    PendingRosterTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRosterTierStatus) {
        if (!canMoveTo(next)) {
            error("pending roster tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending roster tier"} $code"

    fun weightedValue(): Double = round((stopCount.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingRosterTier {
        return PendingRosterTier(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            depot = depot,
            phone = phone,
            weight = weight,
            stopCount = stopCount,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending roster tier in status open. */
fun newPendingRosterTier(code: String, name: String): PendingRosterTier {
    return PendingRosterTier(
        code = code,
        name = name,
        durationMinutes = 0,
        depot = "",
        phone = "",
        weight = 0.0,
        stopCount = 0,
        shiftCode = "",
        status = PendingRosterTierStatus.OPEN,
    )
}
