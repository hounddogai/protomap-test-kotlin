package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal emission factor segment is in its life cycle. */
enum class SeasonalEmissionFactorSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One seasonal emission factor segment, with what the planners need to act on it. */
data class SeasonalEmissionFactorSegment(
    var code: String,
    var name: String,
    var minimum: Double,
    var pallets: Int,
    var routeCode: String,
    var quantity: Int,
    var status: SeasonalEmissionFactorSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorSegmentStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorSegmentStatus.DRAFT -> return next == SeasonalEmissionFactorSegmentStatus.PENDING || next == SeasonalEmissionFactorSegmentStatus.CLOSED
            SeasonalEmissionFactorSegmentStatus.PENDING -> return next == SeasonalEmissionFactorSegmentStatus.SUSPENDED
            SeasonalEmissionFactorSegmentStatus.ACTIVE -> {
                when (next) {
                    SeasonalEmissionFactorSegmentStatus.SUSPENDED, SeasonalEmissionFactorSegmentStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalEmissionFactorSegmentStatus.SUSPENDED -> {
                when (next) {
                    SeasonalEmissionFactorSegmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round(pallets.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): SeasonalEmissionFactorSegment {
        return SeasonalEmissionFactorSegment(
            code = code,
            name = name,
            minimum = minimum,
            pallets = pallets,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal emission factor segment in status draft. */
fun newSeasonalEmissionFactorSegment(code: String, name: String): SeasonalEmissionFactorSegment {
    return SeasonalEmissionFactorSegment(
        code = code,
        name = name,
        minimum = 0.0,
        pallets = 0,
        routeCode = "",
        quantity = 0,
        status = SeasonalEmissionFactorSegmentStatus.DRAFT,
    )
}
