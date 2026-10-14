package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary capacity threshold records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a primary capacity threshold is in its life cycle. */
enum class PrimaryCapacityThresholdStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary capacity threshold, with what the planners need to act on it. */
data class PrimaryCapacityThreshold(
    var code: String,
    var name: String,
    var capacity: Int,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var status: PrimaryCapacityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCapacityThresholdStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCapacityThresholdStatus): Boolean {
        when (status) {
            PrimaryCapacityThresholdStatus.CREATED -> {
                when (next) {
                    PrimaryCapacityThresholdStatus.PUBLISHED -> return true
                }
                return false
            }
            PrimaryCapacityThresholdStatus.VALIDATED -> return next == PrimaryCapacityThresholdStatus.PUBLISHED
            PrimaryCapacityThresholdStatus.PUBLISHED -> return next == PrimaryCapacityThresholdStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryCapacityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary capacity threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((pallets.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCapacityThreshold {
        return PrimaryCapacityThreshold(
            code = code,
            name = name,
            capacity = capacity,
            attributes = attributes,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary capacity threshold in status created. */
fun newPrimaryCapacityThreshold(code: String, name: String): PrimaryCapacityThreshold {
    return PrimaryCapacityThreshold(
        code = code,
        name = name,
        capacity = 0,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        status = PrimaryCapacityThresholdStatus.CREATED,
    )
}
