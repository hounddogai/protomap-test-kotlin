package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy fuel burn entry is in its life cycle. */
enum class LegacyFuelBurnEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy fuel burn entry, with what the planners need to act on it. */
data class LegacyFuelBurnEntry(
    var code: String,
    var name: String,
    var region: String,
    var endsAt: Int,
    var timezone: String,
    var archived: Boolean,
    var attributes: MutableMap<String, String>,
    var durationMinutes: Int,
    var shiftCode: String,
    var status: LegacyFuelBurnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelBurnEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelBurnEntryStatus): Boolean {
        when (status) {
            LegacyFuelBurnEntryStatus.REQUESTED -> return next == LegacyFuelBurnEntryStatus.GRANTED || next == LegacyFuelBurnEntryStatus.REVOKED
            LegacyFuelBurnEntryStatus.GRANTED -> return next == LegacyFuelBurnEntryStatus.REVOKED || next == LegacyFuelBurnEntryStatus.EXPIRED
            LegacyFuelBurnEntryStatus.REVOKED -> return next == LegacyFuelBurnEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyFuelBurnEntryStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel burn entry $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((durationMinutes.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyFuelBurnEntry {
        return LegacyFuelBurnEntry(
            code = code,
            name = name,
            region = region,
            endsAt = endsAt,
            timezone = timezone,
            archived = archived,
            attributes = attributes,
            durationMinutes = durationMinutes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy fuel burn entry in status requested. */
fun newLegacyFuelBurnEntry(code: String, name: String): LegacyFuelBurnEntry {
    return LegacyFuelBurnEntry(
        code = code,
        name = name,
        region = "",
        endsAt = 0,
        timezone = "",
        archived = false,
        attributes = mutableMapOf<String, String>(),
        durationMinutes = 0,
        shiftCode = "",
        status = LegacyFuelBurnEntryStatus.REQUESTED,
    )
}
