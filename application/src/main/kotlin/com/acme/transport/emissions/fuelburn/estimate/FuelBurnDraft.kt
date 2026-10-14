package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn draft is in its life cycle. */
enum class FuelBurnDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fuel burn draft as the fuel burn team keeps it. */
data class FuelBurnDraft(
    var code: String,
    var name: String,
    var active: Boolean,
    var minimum: Double,
    var endsAt: Int,
    var weight: Double,
    var trailerId: String,
    var status: FuelBurnDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnDraftStatus): Boolean {
        when (status) {
            FuelBurnDraftStatus.PROPOSED -> return next == FuelBurnDraftStatus.WITHDRAWN
            FuelBurnDraftStatus.APPROVED -> {
                when (next) {
                    FuelBurnDraftStatus.REJECTED, FuelBurnDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            FuelBurnDraftStatus.REJECTED -> return next == FuelBurnDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FuelBurnDraftStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((minimum + endsAt.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnDraft {
        return FuelBurnDraft(
            code = code,
            name = name,
            active = active,
            minimum = minimum,
            endsAt = endsAt,
            weight = weight,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn draft in status proposed. */
fun newFuelBurnDraft(code: String, name: String): FuelBurnDraft {
    return FuelBurnDraft(
        code = code,
        name = name,
        active = false,
        minimum = 0.0,
        endsAt = 0,
        weight = 0.0,
        trailerId = "",
        status = FuelBurnDraftStatus.PROPOSED,
    )
}
