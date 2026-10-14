package com.acme.transport.linehaul.capacity.capacitymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional capacity mapping records of linehaul's capacity work, and the rules, stores, and calculations
// around them.

/** Where a regional capacity mapping is in its life cycle. */
enum class RegionalCapacityMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a regional capacity mapping and its current status. */
data class RegionalCapacityMapping(
    var code: String,
    var label: String,
    var timezone: String,
    var discount: Double,
    var threshold: Double,
    var source: String,
    var depot: String,
    var status: RegionalCapacityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCapacityMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCapacityMappingStatus): Boolean {
        when (status) {
            RegionalCapacityMappingStatus.PROPOSED -> {
                when (next) {
                    RegionalCapacityMappingStatus.APPROVED, RegionalCapacityMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCapacityMappingStatus.APPROVED -> {
                when (next) {
                    RegionalCapacityMappingStatus.REJECTED -> return true
                }
                return false
            }
            RegionalCapacityMappingStatus.REJECTED -> {
                when (next) {
                    RegionalCapacityMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCapacityMappingStatus) {
        if (!canMoveTo(next)) {
            error("regional capacity mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netLoad(): Double = round((discount - threshold) * 100) / 100

    fun snapshot(): RegionalCapacityMapping {
        return RegionalCapacityMapping(
            code = code,
            label = label,
            timezone = timezone,
            discount = discount,
            threshold = threshold,
            source = source,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional capacity mapping in status proposed. */
fun newRegionalCapacityMapping(code: String, label: String): RegionalCapacityMapping {
    return RegionalCapacityMapping(
        code = code,
        label = label,
        timezone = "",
        discount = 0.0,
        threshold = 0.0,
        source = "",
        depot = "",
        status = RegionalCapacityMappingStatus.PROPOSED,
    )
}
