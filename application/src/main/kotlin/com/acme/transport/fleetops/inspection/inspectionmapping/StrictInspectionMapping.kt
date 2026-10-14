package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict inspection mapping records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a strict inspection mapping is in its life cycle. */
enum class StrictInspectionMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict inspection mapping. Its code is unique within a tenant. */
data class StrictInspectionMapping(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var pallets: Int,
    var expiresAt: Int,
    var trailerId: String,
    var timezone: String,
    var description: String,
    var discount: Double,
    var batchSize: Int,
    var status: StrictInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictInspectionMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictInspectionMappingStatus): Boolean {
        when (status) {
            StrictInspectionMappingStatus.PLANNED -> return next == StrictInspectionMappingStatus.SCHEDULED || next == StrictInspectionMappingStatus.IN_PROGRESS
            StrictInspectionMappingStatus.SCHEDULED -> return next == StrictInspectionMappingStatus.IN_PROGRESS || next == StrictInspectionMappingStatus.COMPLETED
            StrictInspectionMappingStatus.IN_PROGRESS -> return next == StrictInspectionMappingStatus.COMPLETED || next == StrictInspectionMappingStatus.CANCELLED
            StrictInspectionMappingStatus.COMPLETED -> {
                when (next) {
                    StrictInspectionMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict inspection mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict inspection mapping"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((expiresAt.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): StrictInspectionMapping {
        return StrictInspectionMapping(
            code = code,
            title = title,
            attributes = attributes,
            pallets = pallets,
            expiresAt = expiresAt,
            trailerId = trailerId,
            timezone = timezone,
            description = description,
            discount = discount,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict inspection mapping in status planned. */
fun newStrictInspectionMapping(code: String, title: String): StrictInspectionMapping {
    return StrictInspectionMapping(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        pallets = 0,
        expiresAt = 0,
        trailerId = "",
        timezone = "",
        description = "",
        discount = 0.0,
        batchSize = 0,
        status = StrictInspectionMappingStatus.PLANNED,
    )
}
