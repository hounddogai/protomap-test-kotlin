package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling utilization score is in its life cycle. */
enum class RollingUtilizationScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A rolling utilization score as the utilization team keeps it. */
data class RollingUtilizationScore(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var capacity: Int,
    var endsAt: Int,
    var floorValue: Double,
    var status: RollingUtilizationScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingUtilizationScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingUtilizationScoreStatus): Boolean {
        when (status) {
            RollingUtilizationScoreStatus.DRAFT -> return next == RollingUtilizationScoreStatus.ACTIVE || next == RollingUtilizationScoreStatus.SUSPENDED
            RollingUtilizationScoreStatus.PENDING -> {
                when (next) {
                    RollingUtilizationScoreStatus.ACTIVE -> return true
                }
                return false
            }
            RollingUtilizationScoreStatus.ACTIVE -> return next == RollingUtilizationScoreStatus.CLOSED
            RollingUtilizationScoreStatus.SUSPENDED -> {
                when (next) {
                    RollingUtilizationScoreStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingUtilizationScoreStatus) {
        if (!canMoveTo(next)) {
            error("rolling utilization score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(distanceKm * floorValue * 100) / 100

    fun snapshot(): RollingUtilizationScore {
        return RollingUtilizationScore(
            code = code,
            name = name,
            distanceKm = distanceKm,
            capacity = capacity,
            endsAt = endsAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling utilization score in status draft. */
fun newRollingUtilizationScore(code: String, name: String): RollingUtilizationScore {
    return RollingUtilizationScore(
        code = code,
        name = name,
        distanceKm = 0.0,
        capacity = 0,
        endsAt = 0,
        floorValue = 0.0,
        status = RollingUtilizationScoreStatus.DRAFT,
    )
}
