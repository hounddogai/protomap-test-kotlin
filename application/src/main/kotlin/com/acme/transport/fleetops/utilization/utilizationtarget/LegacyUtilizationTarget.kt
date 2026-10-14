package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy utilization target records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a legacy utilization target is in its life cycle. */
enum class LegacyUtilizationTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A legacy utilization target as the utilization team keeps it. */
data class LegacyUtilizationTarget(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var description: String,
    var amount: Double,
    var lane: String,
    var status: LegacyUtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyUtilizationTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyUtilizationTargetStatus): Boolean {
        when (status) {
            LegacyUtilizationTargetStatus.CREATED -> {
                when (next) {
                    LegacyUtilizationTargetStatus.VALIDATED, LegacyUtilizationTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            LegacyUtilizationTargetStatus.VALIDATED -> {
                when (next) {
                    LegacyUtilizationTargetStatus.PUBLISHED, LegacyUtilizationTargetStatus.RETIRED -> return true
                }
                return false
            }
            LegacyUtilizationTargetStatus.PUBLISHED -> {
                when (next) {
                    LegacyUtilizationTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyUtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("legacy utilization target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((fuelLiters + amount) * 100) / 100

    fun snapshot(): LegacyUtilizationTarget {
        return LegacyUtilizationTarget(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            depot = depot,
            attributes = attributes,
            description = description,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy utilization target in status created. */
fun newLegacyUtilizationTarget(code: String, label: String): LegacyUtilizationTarget {
    return LegacyUtilizationTarget(
        code = code,
        label = label,
        fuelLiters = 0.0,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        description = "",
        amount = 0.0,
        lane = "",
        status = LegacyUtilizationTargetStatus.CREATED,
    )
}
