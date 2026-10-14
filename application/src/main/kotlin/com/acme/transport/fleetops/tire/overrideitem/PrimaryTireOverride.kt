package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary tire override records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a primary tire override is in its life cycle. */
enum class PrimaryTireOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A primary tire override. Its code is unique within a tenant. */
data class PrimaryTireOverride(
    var code: String,
    var name: String,
    var attempts: Int,
    var floorValue: Double,
    var driverId: String,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var status: PrimaryTireOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireOverrideStatus): Boolean {
        when (status) {
            PrimaryTireOverrideStatus.DRAFT -> return next == PrimaryTireOverrideStatus.PENDING || next == PrimaryTireOverrideStatus.ACTIVE
            PrimaryTireOverrideStatus.PENDING -> {
                when (next) {
                    PrimaryTireOverrideStatus.CLOSED -> return true
                }
                return false
            }
            PrimaryTireOverrideStatus.ACTIVE -> return next == PrimaryTireOverrideStatus.SUSPENDED || next == PrimaryTireOverrideStatus.CLOSED
            PrimaryTireOverrideStatus.SUSPENDED -> return next == PrimaryTireOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: PrimaryTireOverrideStatus) {
        if (!canMoveTo(next)) {
            error("primary tire override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(attempts.toDouble() * floorValue * 100) / 100

    fun snapshot(): PrimaryTireOverride {
        return PrimaryTireOverride(
            code = code,
            name = name,
            attempts = attempts,
            floorValue = floorValue,
            driverId = driverId,
            budget = budget,
            attributes = attributes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary tire override in status draft. */
fun newPrimaryTireOverride(code: String, name: String): PrimaryTireOverride {
    return PrimaryTireOverride(
        code = code,
        name = name,
        attempts = 0,
        floorValue = 0.0,
        driverId = "",
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        status = PrimaryTireOverrideStatus.DRAFT,
    )
}
