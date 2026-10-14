package com.acme.transport.emissions.fuelburn.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy fuel burn draft is in its life cycle. */
enum class LegacyFuelBurnDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy fuel burn draft. Its code is unique within a tenant. */
data class LegacyFuelBurnDraft(
    var code: String,
    var name: String,
    var floorValue: Double,
    var loadKg: Double,
    var pinned: Boolean,
    var rate: Double,
    var channel: String,
    var weight: Double,
    var status: LegacyFuelBurnDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelBurnDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelBurnDraftStatus): Boolean {
        when (status) {
            LegacyFuelBurnDraftStatus.REQUESTED -> return next == LegacyFuelBurnDraftStatus.GRANTED
            LegacyFuelBurnDraftStatus.GRANTED -> {
                when (next) {
                    LegacyFuelBurnDraftStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyFuelBurnDraftStatus.REVOKED -> {
                when (next) {
                    LegacyFuelBurnDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyFuelBurnDraftStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel burn draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((loadKg + floorValue) * 100) / 100

    fun snapshot(): LegacyFuelBurnDraft {
        return LegacyFuelBurnDraft(
            code = code,
            name = name,
            floorValue = floorValue,
            loadKg = loadKg,
            pinned = pinned,
            rate = rate,
            channel = channel,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy fuel burn draft in status requested. */
fun newLegacyFuelBurnDraft(code: String, name: String): LegacyFuelBurnDraft {
    return LegacyFuelBurnDraft(
        code = code,
        name = name,
        floorValue = 0.0,
        loadKg = 0.0,
        pinned = false,
        rate = 0.0,
        channel = "",
        weight = 0.0,
        status = LegacyFuelBurnDraftStatus.REQUESTED,
    )
}
