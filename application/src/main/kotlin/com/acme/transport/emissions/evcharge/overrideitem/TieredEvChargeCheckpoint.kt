package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge checkpoint is in its life cycle. */
enum class TieredEvChargeCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered ev charge checkpoint as the ev charge team keeps it. */
data class TieredEvChargeCheckpoint(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var score: Double,
    var cost: Double,
    var currency: String,
    var locale: String,
    var category: String,
    var status: TieredEvChargeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargeCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargeCheckpointStatus): Boolean {
        when (status) {
            TieredEvChargeCheckpointStatus.CREATED -> return next == TieredEvChargeCheckpointStatus.VALIDATED || next == TieredEvChargeCheckpointStatus.PUBLISHED
            TieredEvChargeCheckpointStatus.VALIDATED -> {
                when (next) {
                    TieredEvChargeCheckpointStatus.PUBLISHED, TieredEvChargeCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            TieredEvChargeCheckpointStatus.PUBLISHED -> return next == TieredEvChargeCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredEvChargeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered ev charge checkpoint"} $code"

    fun effectiveVolume(): Double = round((distanceKm + cost) * 100) / 100

    fun snapshot(): TieredEvChargeCheckpoint {
        return TieredEvChargeCheckpoint(
            code = code,
            name = name,
            distanceKm = distanceKm,
            score = score,
            cost = cost,
            currency = currency,
            locale = locale,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered ev charge checkpoint in status created. */
fun newTieredEvChargeCheckpoint(code: String, name: String): TieredEvChargeCheckpoint {
    return TieredEvChargeCheckpoint(
        code = code,
        name = name,
        distanceKm = 0.0,
        score = 0.0,
        cost = 0.0,
        currency = "",
        locale = "",
        category = "",
        status = TieredEvChargeCheckpointStatus.CREATED,
    )
}
