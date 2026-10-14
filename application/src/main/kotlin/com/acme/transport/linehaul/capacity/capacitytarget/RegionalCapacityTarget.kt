package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional capacity target records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a regional capacity target is in its life cycle. */
enum class RegionalCapacityTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional capacity target. Its code is unique within a tenant. */
data class RegionalCapacityTarget(
    var code: String,
    var name: String,
    var archived: Boolean,
    var unit: String,
    var shiftCode: String,
    var minimum: Double,
    var loadKg: Double,
    var status: RegionalCapacityTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityTargetStatus): Boolean {
        when (status) {
            RegionalCapacityTargetStatus.CREATED -> {
                when (next) {
                    RegionalCapacityTargetStatus.VALIDATED, RegionalCapacityTargetStatus.RETIRED -> return true
                }
                return false
            }
            RegionalCapacityTargetStatus.VALIDATED -> return next == RegionalCapacityTargetStatus.PUBLISHED || next == RegionalCapacityTargetStatus.RETIRED
            RegionalCapacityTargetStatus.PUBLISHED -> return next == RegionalCapacityTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalCapacityTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((minimum + loadKg) * 100) / 100

    fun snapshot(): RegionalCapacityTarget {
        return RegionalCapacityTarget(
            code = code,
            name = name,
            archived = archived,
            unit = unit,
            shiftCode = shiftCode,
            minimum = minimum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional capacity target in status created. */
fun newRegionalCapacityTarget(code: String, name: String): RegionalCapacityTarget {
    return RegionalCapacityTarget(
        code = code,
        name = name,
        archived = false,
        unit = "",
        shiftCode = "",
        minimum = 0.0,
        loadKg = 0.0,
        status = RegionalCapacityTargetStatus.CREATED,
    )
}
