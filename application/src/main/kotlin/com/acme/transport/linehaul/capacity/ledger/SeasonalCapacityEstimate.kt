package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal capacity estimate is in its life cycle. */
enum class SeasonalCapacityEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal capacity estimate, with what the planners need to act on it. */
data class SeasonalCapacityEstimate(
    var code: String,
    var name: String,
    var rate: Double,
    var updatedAt: Int,
    var createdAt: Int,
    var checksum: String,
    var discount: Double,
    var retries: Int,
    var fuelLiters: Double,
    var region: String,
    var status: SeasonalCapacityEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacityEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacityEstimateStatus): Boolean {
        when (status) {
            SeasonalCapacityEstimateStatus.OPEN -> {
                when (next) {
                    SeasonalCapacityEstimateStatus.IN_REVIEW -> return true
                }
                return false
            }
            SeasonalCapacityEstimateStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalCapacityEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalCapacityEstimateStatus.RESOLVED -> {
                when (next) {
                    SeasonalCapacityEstimateStatus.REOPENED, SeasonalCapacityEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalCapacityEstimateStatus.REOPENED -> return next == SeasonalCapacityEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalCapacityEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal capacity estimate"} $code"

    fun effectiveExposure(): Double = round((retries.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): SeasonalCapacityEstimate {
        return SeasonalCapacityEstimate(
            code = code,
            name = name,
            rate = rate,
            updatedAt = updatedAt,
            createdAt = createdAt,
            checksum = checksum,
            discount = discount,
            retries = retries,
            fuelLiters = fuelLiters,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal capacity estimate in status open. */
fun newSeasonalCapacityEstimate(code: String, name: String): SeasonalCapacityEstimate {
    return SeasonalCapacityEstimate(
        code = code,
        name = name,
        rate = 0.0,
        updatedAt = 0,
        createdAt = 0,
        checksum = "",
        discount = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        region = "",
        status = SeasonalCapacityEstimateStatus.OPEN,
    )
}
