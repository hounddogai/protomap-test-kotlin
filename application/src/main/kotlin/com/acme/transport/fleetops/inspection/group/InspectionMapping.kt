package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize

/** Where a inspection mapping is in its life cycle. */
enum class InspectionMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection mapping as the inspection team keeps it. */
data class InspectionMapping(
    var code: String,
    var name: String,
    var notes: String,
    var approved: Boolean,
    var priority: Int,
    var category: String,
    var locale: String,
    var status: InspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMappingStatus): Boolean {
        when (status) {
            InspectionMappingStatus.DRAFT -> return next == InspectionMappingStatus.SUSPENDED
            InspectionMappingStatus.PENDING -> {
                when (next) {
                    InspectionMappingStatus.SUSPENDED, InspectionMappingStatus.CLOSED -> return true
                }
                return false
            }
            InspectionMappingStatus.ACTIVE -> {
                when (next) {
                    InspectionMappingStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionMappingStatus.SUSPENDED -> {
                when (next) {
                    InspectionMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("inspection mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): InspectionMapping {
        return InspectionMapping(
            code = code,
            name = name,
            notes = notes,
            approved = approved,
            priority = priority,
            category = category,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a inspection mapping in status draft. */
fun newInspectionMapping(code: String, name: String): InspectionMapping {
    return InspectionMapping(
        code = code,
        name = name,
        notes = "",
        approved = false,
        priority = 0,
        category = "",
        locale = "",
        status = InspectionMappingStatus.DRAFT,
    )
}
