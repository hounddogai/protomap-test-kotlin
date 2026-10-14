package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped locker score records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a capped locker score is in its life cycle. */
enum class CappedLockerScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped locker score as the locker team keeps it. */
data class CappedLockerScore(
    var code: String,
    var label: String,
    var windowDays: Int,
    var graceDays: Int,
    var category: String,
    var status: CappedLockerScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLockerScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLockerScoreStatus): Boolean {
        when (status) {
            CappedLockerScoreStatus.DRAFT -> return next == CappedLockerScoreStatus.SUSPENDED
            CappedLockerScoreStatus.PENDING -> {
                when (next) {
                    CappedLockerScoreStatus.ACTIVE -> return true
                }
                return false
            }
            CappedLockerScoreStatus.ACTIVE -> {
                when (next) {
                    CappedLockerScoreStatus.SUSPENDED, CappedLockerScoreStatus.CLOSED -> return true
                }
                return false
            }
            CappedLockerScoreStatus.SUSPENDED -> return next == CappedLockerScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedLockerScoreStatus) {
        if (!canMoveTo(next)) {
            error("capped locker score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((windowDays.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): CappedLockerScore {
        return CappedLockerScore(
            code = code,
            label = label,
            windowDays = windowDays,
            graceDays = graceDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped locker score in status draft. */
fun newCappedLockerScore(code: String, label: String): CappedLockerScore {
    return CappedLockerScore(
        code = code,
        label = label,
        windowDays = 0,
        graceDays = 0,
        category = "",
        status = CappedLockerScoreStatus.DRAFT,
    )
}
