package com.acme.transport.crewing.roster.rostercycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback roster cycle records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a fallback roster cycle is in its life cycle. */
enum class FallbackRosterCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback roster cycle, with what the planners need to act on it. */
data class FallbackRosterCycle(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var carrier: String,
    var driverId: String,
    var pallets: Int,
    var ratio: Double,
    var endsAt: Int,
    var minimum: Double,
    var graceDays: Int,
    var status: FallbackRosterCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRosterCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRosterCycleStatus): Boolean {
        when (status) {
            FallbackRosterCycleStatus.DRAFT -> return next == FallbackRosterCycleStatus.PENDING || next == FallbackRosterCycleStatus.ACTIVE
            FallbackRosterCycleStatus.PENDING -> {
                when (next) {
                    FallbackRosterCycleStatus.ACTIVE, FallbackRosterCycleStatus.CLOSED -> return true
                }
                return false
            }
            FallbackRosterCycleStatus.ACTIVE -> return next == FallbackRosterCycleStatus.SUSPENDED
            FallbackRosterCycleStatus.SUSPENDED -> {
                when (next) {
                    FallbackRosterCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRosterCycleStatus) {
        if (!canMoveTo(next)) {
            error("fallback roster cycle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback roster cycle"} $code"

    fun weightedVolume(): Double = round(ratio * graceDays.toDouble() * 100) / 100

    fun snapshot(): FallbackRosterCycle {
        return FallbackRosterCycle(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            carrier = carrier,
            driverId = driverId,
            pallets = pallets,
            ratio = ratio,
            endsAt = endsAt,
            minimum = minimum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback roster cycle in status draft. */
fun newFallbackRosterCycle(code: String, title: String): FallbackRosterCycle {
    return FallbackRosterCycle(
        code = code,
        title = title,
        etaMinutes = 0,
        carrier = "",
        driverId = "",
        pallets = 0,
        ratio = 0.0,
        endsAt = 0,
        minimum = 0.0,
        graceDays = 0,
        status = FallbackRosterCycleStatus.DRAFT,
    )
}
