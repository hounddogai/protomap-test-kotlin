package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global inspection mapping records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a global inspection mapping is in its life cycle. */
enum class GlobalInspectionMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global inspection mapping as the inspection team keeps it. */
data class GlobalInspectionMapping(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var distanceKm: Double,
    var ratio: Double,
    var revision: Int,
    var pallets: Int,
    var tags: MutableList<String>,
    var loadKg: Double,
    var status: GlobalInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionMappingStatus): Boolean {
        when (status) {
            GlobalInspectionMappingStatus.OPEN -> return next == GlobalInspectionMappingStatus.IN_REVIEW || next == GlobalInspectionMappingStatus.RESOLVED
            GlobalInspectionMappingStatus.IN_REVIEW -> {
                when (next) {
                    GlobalInspectionMappingStatus.REOPENED -> return true
                }
                return false
            }
            GlobalInspectionMappingStatus.RESOLVED -> return next == GlobalInspectionMappingStatus.REOPENED || next == GlobalInspectionMappingStatus.ARCHIVED
            GlobalInspectionMappingStatus.REOPENED -> return next == GlobalInspectionMappingStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: GlobalInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("global inspection mapping $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round((loadKg + ratio) * 100) / 100

    fun snapshot(): GlobalInspectionMapping {
        return GlobalInspectionMapping(
            code = code,
            name = name,
            attributes = attributes,
            distanceKm = distanceKm,
            ratio = ratio,
            revision = revision,
            pallets = pallets,
            tags = tags,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global inspection mapping in status open. */
fun newGlobalInspectionMapping(code: String, name: String): GlobalInspectionMapping {
    return GlobalInspectionMapping(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        distanceKm = 0.0,
        ratio = 0.0,
        revision = 0,
        pallets = 0,
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        status = GlobalInspectionMappingStatus.OPEN,
    )
}
