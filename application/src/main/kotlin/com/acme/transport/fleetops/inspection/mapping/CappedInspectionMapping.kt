package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped inspection mapping records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a capped inspection mapping is in its life cycle. */
enum class CappedInspectionMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped inspection mapping and its current status. */
data class CappedInspectionMapping(
    var code: String,
    var title: String,
    var sequence: Int,
    var active: Boolean,
    var weight: Double,
    var description: String,
    var ownerId: String,
    var status: CappedInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionMappingStatus): Boolean {
        when (status) {
            CappedInspectionMappingStatus.REQUESTED -> {
                when (next) {
                    CappedInspectionMappingStatus.GRANTED, CappedInspectionMappingStatus.EXPIRED -> return true
                }
                return false
            }
            CappedInspectionMappingStatus.GRANTED -> return next == CappedInspectionMappingStatus.REVOKED
            CappedInspectionMappingStatus.REVOKED -> {
                when (next) {
                    CappedInspectionMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((sequence.toDouble() - weight) * 100) / 100

    fun snapshot(): CappedInspectionMapping {
        return CappedInspectionMapping(
            code = code,
            title = title,
            sequence = sequence,
            active = active,
            weight = weight,
            description = description,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped inspection mapping in status requested. */
fun newCappedInspectionMapping(code: String, title: String): CappedInspectionMapping {
    return CappedInspectionMapping(
        code = code,
        title = title,
        sequence = 0,
        active = false,
        weight = 0.0,
        description = "",
        ownerId = "",
        status = CappedInspectionMappingStatus.REQUESTED,
    )
}
