package com.acme.transport.network.zone.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive zone allocation is in its life cycle. */
enum class AdaptiveZoneAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a adaptive zone allocation and its current status. */
data class AdaptiveZoneAllocation(
    var code: String,
    var name: String,
    var label: String,
    var graceDays: Int,
    var source: String,
    var shiftCode: String,
    var revision: Int,
    var loadKg: Double,
    var cost: Double,
    var status: AdaptiveZoneAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveZoneAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveZoneAllocationStatus): Boolean {
        when (status) {
            AdaptiveZoneAllocationStatus.CREATED -> return next == AdaptiveZoneAllocationStatus.VALIDATED || next == AdaptiveZoneAllocationStatus.PUBLISHED
            AdaptiveZoneAllocationStatus.VALIDATED -> {
                when (next) {
                    AdaptiveZoneAllocationStatus.PUBLISHED, AdaptiveZoneAllocationStatus.RETIRED -> return true
                }
                return false
            }
            AdaptiveZoneAllocationStatus.PUBLISHED -> {
                when (next) {
                    AdaptiveZoneAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveZoneAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive zone allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($name)"

    fun weightedVolume(): Double = round(loadKg * cost * 100) / 100

    fun snapshot(): AdaptiveZoneAllocation {
        return AdaptiveZoneAllocation(
            code = code,
            name = name,
            label = label,
            graceDays = graceDays,
            source = source,
            shiftCode = shiftCode,
            revision = revision,
            loadKg = loadKg,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive zone allocation in status created. */
fun newAdaptiveZoneAllocation(code: String, name: String): AdaptiveZoneAllocation {
    return AdaptiveZoneAllocation(
        code = code,
        name = name,
        label = "",
        graceDays = 0,
        source = "",
        shiftCode = "",
        revision = 0,
        loadKg = 0.0,
        cost = 0.0,
        status = AdaptiveZoneAllocationStatus.CREATED,
    )
}
