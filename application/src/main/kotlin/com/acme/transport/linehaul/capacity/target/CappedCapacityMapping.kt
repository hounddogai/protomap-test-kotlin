package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped capacity mapping is in its life cycle. */
enum class CappedCapacityMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped capacity mapping, with what the planners need to act on it. */
data class CappedCapacityMapping(
    var code: String,
    var label: String,
    var approved: Boolean,
    var reviewer: String,
    var currency: String,
    var trailerId: String,
    var sequence: Int,
    var etaMinutes: Int,
    var pinned: Boolean,
    var active: Boolean,
    var status: CappedCapacityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityMappingStatus): Boolean {
        when (status) {
            CappedCapacityMappingStatus.CREATED -> return next == CappedCapacityMappingStatus.PUBLISHED
            CappedCapacityMappingStatus.VALIDATED -> return next == CappedCapacityMappingStatus.PUBLISHED || next == CappedCapacityMappingStatus.RETIRED
            CappedCapacityMappingStatus.PUBLISHED -> return next == CappedCapacityMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedCapacityMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round(sequence.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CappedCapacityMapping {
        return CappedCapacityMapping(
            code = code,
            label = label,
            approved = approved,
            reviewer = reviewer,
            currency = currency,
            trailerId = trailerId,
            sequence = sequence,
            etaMinutes = etaMinutes,
            pinned = pinned,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped capacity mapping in status created. */
fun newCappedCapacityMapping(code: String, label: String): CappedCapacityMapping {
    return CappedCapacityMapping(
        code = code,
        label = label,
        approved = false,
        reviewer = "",
        currency = "",
        trailerId = "",
        sequence = 0,
        etaMinutes = 0,
        pinned = false,
        active = false,
        status = CappedCapacityMappingStatus.CREATED,
    )
}
