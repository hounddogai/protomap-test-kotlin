package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global ev charge override is in its life cycle. */
enum class GlobalEvChargeOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global ev charge override as the ev charge team keeps it. */
data class GlobalEvChargeOverride(
    var code: String,
    var name: String,
    var archived: Boolean,
    var sequence: Int,
    var reviewer: String,
    var locale: String,
    var segment: String,
    var fuelLiters: Double,
    var etaMinutes: Int,
    var active: Boolean,
    var status: GlobalEvChargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalEvChargeOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalEvChargeOverrideStatus): Boolean {
        when (status) {
            GlobalEvChargeOverrideStatus.REQUESTED -> {
                when (next) {
                    GlobalEvChargeOverrideStatus.GRANTED, GlobalEvChargeOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalEvChargeOverrideStatus.GRANTED -> {
                when (next) {
                    GlobalEvChargeOverrideStatus.REVOKED, GlobalEvChargeOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalEvChargeOverrideStatus.REVOKED -> {
                when (next) {
                    GlobalEvChargeOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalEvChargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("global ev charge override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((sequence.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): GlobalEvChargeOverride {
        return GlobalEvChargeOverride(
            code = code,
            name = name,
            archived = archived,
            sequence = sequence,
            reviewer = reviewer,
            locale = locale,
            segment = segment,
            fuelLiters = fuelLiters,
            etaMinutes = etaMinutes,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a global ev charge override in status requested. */
fun newGlobalEvChargeOverride(code: String, name: String): GlobalEvChargeOverride {
    return GlobalEvChargeOverride(
        code = code,
        name = name,
        archived = false,
        sequence = 0,
        reviewer = "",
        locale = "",
        segment = "",
        fuelLiters = 0.0,
        etaMinutes = 0,
        active = false,
        status = GlobalEvChargeOverrideStatus.REQUESTED,
    )
}
