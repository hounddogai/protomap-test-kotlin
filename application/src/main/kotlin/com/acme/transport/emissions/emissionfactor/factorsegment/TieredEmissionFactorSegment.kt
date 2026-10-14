package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered emission factor segment records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a tiered emission factor segment is in its life cycle. */
enum class TieredEmissionFactorSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered emission factor segment, with what the planners need to act on it. */
data class TieredEmissionFactorSegment(
    var code: String,
    var name: String,
    var depot: String,
    var cost: Double,
    var batchSize: Int,
    var revision: Int,
    var score: Double,
    var timezone: String,
    var expiresAt: Int,
    var discount: Double,
    var pallets: Int,
    var status: TieredEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEmissionFactorSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEmissionFactorSegmentStatus): Boolean {
        when (status) {
            TieredEmissionFactorSegmentStatus.PLANNED -> {
                when (next) {
                    TieredEmissionFactorSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            TieredEmissionFactorSegmentStatus.SCHEDULED -> {
                when (next) {
                    TieredEmissionFactorSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            TieredEmissionFactorSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    TieredEmissionFactorSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            TieredEmissionFactorSegmentStatus.COMPLETED -> return next == TieredEmissionFactorSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered emission factor segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((score - discount) * 100) / 100

    fun snapshot(): TieredEmissionFactorSegment {
        return TieredEmissionFactorSegment(
            code = code,
            name = name,
            depot = depot,
            cost = cost,
            batchSize = batchSize,
            revision = revision,
            score = score,
            timezone = timezone,
            expiresAt = expiresAt,
            discount = discount,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered emission factor segment in status planned. */
fun newTieredEmissionFactorSegment(code: String, name: String): TieredEmissionFactorSegment {
    return TieredEmissionFactorSegment(
        code = code,
        name = name,
        depot = "",
        cost = 0.0,
        batchSize = 0,
        revision = 0,
        score = 0.0,
        timezone = "",
        expiresAt = 0,
        discount = 0.0,
        pallets = 0,
        status = TieredEmissionFactorSegmentStatus.PLANNED,
    )
}
