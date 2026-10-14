package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dock door mapping is in its life cycle. */
enum class ScheduledDockDoorMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled dock door mapping and its current status. */
data class ScheduledDockDoorMapping(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var phone: String,
    var amount: Double,
    var country: String,
    var unit: String,
    var durationMinutes: Int,
    var ceiling: Double,
    var sequence: Int,
    var status: ScheduledDockDoorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorMappingStatus): Boolean {
        when (status) {
            ScheduledDockDoorMappingStatus.PLANNED -> return next == ScheduledDockDoorMappingStatus.COMPLETED
            ScheduledDockDoorMappingStatus.SCHEDULED -> {
                when (next) {
                    ScheduledDockDoorMappingStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledDockDoorMappingStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledDockDoorMappingStatus.COMPLETED, ScheduledDockDoorMappingStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledDockDoorMappingStatus.COMPLETED -> return next == ScheduledDockDoorMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorMappingStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((amount + ceiling) * 100) / 100

    fun snapshot(): ScheduledDockDoorMapping {
        return ScheduledDockDoorMapping(
            code = code,
            name = name,
            attributes = attributes,
            phone = phone,
            amount = amount,
            country = country,
            unit = unit,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled dock door mapping in status planned. */
fun newScheduledDockDoorMapping(code: String, name: String): ScheduledDockDoorMapping {
    return ScheduledDockDoorMapping(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        phone = "",
        amount = 0.0,
        country = "",
        unit = "",
        durationMinutes = 0,
        ceiling = 0.0,
        sequence = 0,
        status = ScheduledDockDoorMappingStatus.PLANNED,
    )
}
