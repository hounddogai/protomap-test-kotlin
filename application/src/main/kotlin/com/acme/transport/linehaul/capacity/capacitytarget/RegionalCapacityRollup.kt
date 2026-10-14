package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional capacity rollup is in its life cycle. */
enum class RegionalCapacityRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional capacity rollup as the capacity team keeps it. */
data class RegionalCapacityRollup(
    var code: String,
    var label: String,
    var sequence: Int,
    var fuelLiters: Double,
    var unit: String,
    var ownerId: String,
    var windowDays: Int,
    var status: RegionalCapacityRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityRollupStatus): Boolean {
        when (status) {
            RegionalCapacityRollupStatus.NEW -> {
                when (next) {
                    RegionalCapacityRollupStatus.FAILED -> return true
                }
                return false
            }
            RegionalCapacityRollupStatus.QUEUED -> return next == RegionalCapacityRollupStatus.DONE
            RegionalCapacityRollupStatus.RUNNING -> {
                when (next) {
                    RegionalCapacityRollupStatus.DONE -> return true
                }
                return false
            }
            RegionalCapacityRollupStatus.DONE -> {
                when (next) {
                    RegionalCapacityRollupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCapacityRollupStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((fuelLiters + sequence.toDouble()) * 100) / 100

    fun snapshot(): RegionalCapacityRollup {
        return RegionalCapacityRollup(
            code = code,
            label = label,
            sequence = sequence,
            fuelLiters = fuelLiters,
            unit = unit,
            ownerId = ownerId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional capacity rollup in status new. */
fun newRegionalCapacityRollup(code: String, label: String): RegionalCapacityRollup {
    return RegionalCapacityRollup(
        code = code,
        label = label,
        sequence = 0,
        fuelLiters = 0.0,
        unit = "",
        ownerId = "",
        windowDays = 0,
        status = RegionalCapacityRollupStatus.NEW,
    )
}
