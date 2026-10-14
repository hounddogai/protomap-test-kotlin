package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection group records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection group is in its life cycle. */
enum class InspectionGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a inspection group and its current status. */
data class InspectionGroup(
    var code: String,
    var name: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var ownerId: String,
    var visible: Boolean,
    var updatedAt: Int,
    var etaMinutes: Int,
    var segment: String,
    var checksum: String,
    var status: InspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionGroupStatus): Boolean {
        when (status) {
            InspectionGroupStatus.PROPOSED -> {
                when (next) {
                    InspectionGroupStatus.APPROVED, InspectionGroupStatus.REJECTED -> return true
                }
                return false
            }
            InspectionGroupStatus.APPROVED -> {
                when (next) {
                    InspectionGroupStatus.REJECTED, InspectionGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            InspectionGroupStatus.REJECTED -> return next == InspectionGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("inspection group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"inspection group"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((etaMinutes.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionGroup {
        return InspectionGroup(
            code = code,
            name = name,
            shiftCode = shiftCode,
            attributes = attributes,
            ownerId = ownerId,
            visible = visible,
            updatedAt = updatedAt,
            etaMinutes = etaMinutes,
            segment = segment,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a inspection group in status proposed. */
fun newInspectionGroup(code: String, name: String): InspectionGroup {
    return InspectionGroup(
        code = code,
        name = name,
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        ownerId = "",
        visible = false,
        updatedAt = 0,
        etaMinutes = 0,
        segment = "",
        checksum = "",
        status = InspectionGroupStatus.PROPOSED,
    )
}
