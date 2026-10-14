package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The clearance tier records of customs's clearance work, and the rules, stores, and calculations around
// them.

/** Where a clearance tier is in its life cycle. */
enum class ClearanceTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One clearance tier, with what the planners need to act on it. */
data class ClearanceTier(
    var code: String,
    var label: String,
    var contactEmail: String,
    var minimum: Double,
    var sequence: Int,
    var fuelLiters: Double,
    var region: String,
    var active: Boolean,
    var threshold: Double,
    var odometer: Int,
    var notes: String,
    var status: ClearanceTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceTierStatus): Boolean {
        when (status) {
            ClearanceTierStatus.REQUESTED -> {
                when (next) {
                    ClearanceTierStatus.GRANTED, ClearanceTierStatus.REVOKED -> return true
                }
                return false
            }
            ClearanceTierStatus.GRANTED -> {
                when (next) {
                    ClearanceTierStatus.REVOKED -> return true
                }
                return false
            }
            ClearanceTierStatus.REVOKED -> {
                when (next) {
                    ClearanceTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceTierStatus) {
        if (!canMoveTo(next)) {
            error("clearance tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"clearance tier"} $code"

    fun adjustedLoad(): Double = round(sequence.toDouble() * minimum * 100) / 100

    fun snapshot(): ClearanceTier {
        return ClearanceTier(
            code = code,
            label = label,
            contactEmail = contactEmail,
            minimum = minimum,
            sequence = sequence,
            fuelLiters = fuelLiters,
            region = region,
            active = active,
            threshold = threshold,
            odometer = odometer,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a clearance tier in status requested. */
fun newClearanceTier(code: String, label: String): ClearanceTier {
    return ClearanceTier(
        code = code,
        label = label,
        contactEmail = "",
        minimum = 0.0,
        sequence = 0,
        fuelLiters = 0.0,
        region = "",
        active = false,
        threshold = 0.0,
        odometer = 0,
        notes = "",
        status = ClearanceTierStatus.REQUESTED,
    )
}
