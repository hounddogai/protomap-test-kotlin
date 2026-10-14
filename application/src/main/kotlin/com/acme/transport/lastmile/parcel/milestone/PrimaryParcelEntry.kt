package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary parcel entry is in its life cycle. */
enum class PrimaryParcelEntryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary parcel entry, with what the planners need to act on it. */
data class PrimaryParcelEntry(
    var code: String,
    var name: String,
    var routeCode: String,
    var durationMinutes: Int,
    var weight: Double,
    var vehicleId: String,
    var stopCount: Int,
    var maximum: Double,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var status: PrimaryParcelEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryParcelEntryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryParcelEntryStatus): Boolean {
        when (status) {
            PrimaryParcelEntryStatus.CREATED -> {
                when (next) {
                    PrimaryParcelEntryStatus.PUBLISHED, PrimaryParcelEntryStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryParcelEntryStatus.VALIDATED -> return next == PrimaryParcelEntryStatus.RETIRED
            PrimaryParcelEntryStatus.PUBLISHED -> return next == PrimaryParcelEntryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryParcelEntryStatus) {
        if (!canMoveTo(next)) {
            error("primary parcel entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((weight - maximum) * 100) / 100

    fun snapshot(): PrimaryParcelEntry {
        return PrimaryParcelEntry(
            code = code,
            name = name,
            routeCode = routeCode,
            durationMinutes = durationMinutes,
            weight = weight,
            vehicleId = vehicleId,
            stopCount = stopCount,
            maximum = maximum,
            budget = budget,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary parcel entry in status created. */
fun newPrimaryParcelEntry(code: String, name: String): PrimaryParcelEntry {
    return PrimaryParcelEntry(
        code = code,
        name = name,
        routeCode = "",
        durationMinutes = 0,
        weight = 0.0,
        vehicleId = "",
        stopCount = 0,
        maximum = 0.0,
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = PrimaryParcelEntryStatus.CREATED,
    )
}
