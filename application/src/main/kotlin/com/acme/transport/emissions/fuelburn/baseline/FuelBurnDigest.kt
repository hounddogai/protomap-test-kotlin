package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize

/** Where a fuel burn digest is in its life cycle. */
enum class FuelBurnDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fuel burn digest as the fuel burn team keeps it. */
data class FuelBurnDigest(
    var code: String,
    var name: String,
    var ownerId: String,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var active: Boolean,
    var label: String,
    var weight: Double,
    var status: FuelBurnDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnDigestStatus): Boolean {
        when (status) {
            FuelBurnDigestStatus.PROPOSED -> return next == FuelBurnDigestStatus.APPROVED || next == FuelBurnDigestStatus.REJECTED
            FuelBurnDigestStatus.APPROVED -> {
                when (next) {
                    FuelBurnDigestStatus.REJECTED, FuelBurnDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
            FuelBurnDigestStatus.REJECTED -> {
                when (next) {
                    FuelBurnDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnDigestStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = weight

    fun snapshot(): FuelBurnDigest {
        return FuelBurnDigest(
            code = code,
            name = name,
            ownerId = ownerId,
            attributes = attributes,
            approved = approved,
            active = active,
            label = label,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fuel burn digest in status proposed. */
fun newFuelBurnDigest(code: String, name: String): FuelBurnDigest {
    return FuelBurnDigest(
        code = code,
        name = name,
        ownerId = "",
        attributes = mutableMapOf<String, String>(),
        approved = false,
        active = false,
        label = "",
        weight = 0.0,
        status = FuelBurnDigestStatus.PROPOSED,
    )
}
