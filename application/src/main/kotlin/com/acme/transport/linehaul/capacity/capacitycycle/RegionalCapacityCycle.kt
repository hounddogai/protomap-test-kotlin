package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional capacity cycle records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a regional capacity cycle is in its life cycle. */
enum class RegionalCapacityCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional capacity cycle, with what the planners need to act on it. */
data class RegionalCapacityCycle(
    var code: String,
    var label: String,
    var rate: Double,
    var fuelLiters: Double,
    var currency: String,
    var discount: Double,
    var ratio: Double,
    var checksum: String,
    var depot: String,
    var status: RegionalCapacityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityCycleStatus): Boolean {
        when (status) {
            RegionalCapacityCycleStatus.OPEN -> {
                when (next) {
                    RegionalCapacityCycleStatus.REOPENED -> return true
                }
                return false
            }
            RegionalCapacityCycleStatus.IN_REVIEW -> {
                when (next) {
                    RegionalCapacityCycleStatus.REOPENED, RegionalCapacityCycleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalCapacityCycleStatus.RESOLVED -> return next == RegionalCapacityCycleStatus.REOPENED
            RegionalCapacityCycleStatus.REOPENED -> {
                when (next) {
                    RegionalCapacityCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCapacityCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((rate + ratio) * 100) / 100

    fun snapshot(): RegionalCapacityCycle {
        return RegionalCapacityCycle(
            code = code,
            label = label,
            rate = rate,
            fuelLiters = fuelLiters,
            currency = currency,
            discount = discount,
            ratio = ratio,
            checksum = checksum,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional capacity cycle in status open. */
fun newRegionalCapacityCycle(code: String, label: String): RegionalCapacityCycle {
    return RegionalCapacityCycle(
        code = code,
        label = label,
        rate = 0.0,
        fuelLiters = 0.0,
        currency = "",
        discount = 0.0,
        ratio = 0.0,
        checksum = "",
        depot = "",
        status = RegionalCapacityCycleStatus.OPEN,
    )
}
