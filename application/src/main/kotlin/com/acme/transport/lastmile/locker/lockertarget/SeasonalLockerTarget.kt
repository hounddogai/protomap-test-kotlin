package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal locker target records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a seasonal locker target is in its life cycle. */
enum class SeasonalLockerTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal locker target and its current status. */
data class SeasonalLockerTarget(
    var code: String,
    var name: String,
    var retries: Int,
    var odometer: Int,
    var graceDays: Int,
    var startsAt: Int,
    var checksum: String,
    var createdAt: Int,
    var threshold: Double,
    var status: SeasonalLockerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLockerTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLockerTargetStatus): Boolean {
        when (status) {
            SeasonalLockerTargetStatus.DRAFT -> return next == SeasonalLockerTargetStatus.PENDING
            SeasonalLockerTargetStatus.PENDING -> return next == SeasonalLockerTargetStatus.ACTIVE || next == SeasonalLockerTargetStatus.SUSPENDED
            SeasonalLockerTargetStatus.ACTIVE -> return next == SeasonalLockerTargetStatus.SUSPENDED || next == SeasonalLockerTargetStatus.CLOSED
            SeasonalLockerTargetStatus.SUSPENDED -> {
                when (next) {
                    SeasonalLockerTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalLockerTargetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal locker target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal locker target"} $code"

    fun projectedVolume(): Double = round((odometer.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): SeasonalLockerTarget {
        return SeasonalLockerTarget(
            code = code,
            name = name,
            retries = retries,
            odometer = odometer,
            graceDays = graceDays,
            startsAt = startsAt,
            checksum = checksum,
            createdAt = createdAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal locker target in status draft. */
fun newSeasonalLockerTarget(code: String, name: String): SeasonalLockerTarget {
    return SeasonalLockerTarget(
        code = code,
        name = name,
        retries = 0,
        odometer = 0,
        graceDays = 0,
        startsAt = 0,
        checksum = "",
        createdAt = 0,
        threshold = 0.0,
        status = SeasonalLockerTargetStatus.DRAFT,
    )
}
