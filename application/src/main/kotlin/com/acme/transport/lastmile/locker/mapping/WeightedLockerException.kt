package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted locker exception is in its life cycle. */
enum class WeightedLockerExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a weighted locker exception and its current status. */
data class WeightedLockerException(
    var code: String,
    var name: String,
    var endsAt: Int,
    var trailerId: String,
    var stopCount: Int,
    var distanceKm: Double,
    var priority: Int,
    var status: WeightedLockerExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLockerExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLockerExceptionStatus): Boolean {
        when (status) {
            WeightedLockerExceptionStatus.PROPOSED -> {
                when (next) {
                    WeightedLockerExceptionStatus.REJECTED -> return true
                }
                return false
            }
            WeightedLockerExceptionStatus.APPROVED -> {
                when (next) {
                    WeightedLockerExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedLockerExceptionStatus.REJECTED -> {
                when (next) {
                    WeightedLockerExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLockerExceptionStatus) {
        if (!canMoveTo(next)) {
            error("weighted locker exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round(stopCount.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): WeightedLockerException {
        return WeightedLockerException(
            code = code,
            name = name,
            endsAt = endsAt,
            trailerId = trailerId,
            stopCount = stopCount,
            distanceKm = distanceKm,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted locker exception in status proposed. */
fun newWeightedLockerException(code: String, name: String): WeightedLockerException {
    return WeightedLockerException(
        code = code,
        name = name,
        endsAt = 0,
        trailerId = "",
        stopCount = 0,
        distanceKm = 0.0,
        priority = 0,
        status = WeightedLockerExceptionStatus.PROPOSED,
    )
}
