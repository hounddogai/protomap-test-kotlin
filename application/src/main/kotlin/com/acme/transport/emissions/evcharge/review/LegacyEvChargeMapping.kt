package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy ev charge mapping is in its life cycle. */
enum class LegacyEvChargeMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy ev charge mapping. Its code is unique within a tenant. */
data class LegacyEvChargeMapping(
    var code: String,
    var name: String,
    var depot: String,
    var cost: Double,
    var capacity: Int,
    var ceiling: Double,
    var status: LegacyEvChargeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyEvChargeMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyEvChargeMappingStatus): Boolean {
        when (status) {
            LegacyEvChargeMappingStatus.IDLE -> return next == LegacyEvChargeMappingStatus.DRAINING
            LegacyEvChargeMappingStatus.WARMING -> {
                when (next) {
                    LegacyEvChargeMappingStatus.READY -> return true
                }
                return false
            }
            LegacyEvChargeMappingStatus.READY -> {
                when (next) {
                    LegacyEvChargeMappingStatus.DRAINING -> return true
                }
                return false
            }
            LegacyEvChargeMappingStatus.DRAINING -> {
                when (next) {
                    LegacyEvChargeMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyEvChargeMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy ev charge mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(ceiling * capacity.toDouble() * 100) / 100

    fun snapshot(): LegacyEvChargeMapping {
        return LegacyEvChargeMapping(
            code = code,
            name = name,
            depot = depot,
            cost = cost,
            capacity = capacity,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy ev charge mapping in status idle. */
fun newLegacyEvChargeMapping(code: String, name: String): LegacyEvChargeMapping {
    return LegacyEvChargeMapping(
        code = code,
        name = name,
        depot = "",
        cost = 0.0,
        capacity = 0,
        ceiling = 0.0,
        status = LegacyEvChargeMappingStatus.IDLE,
    )
}
