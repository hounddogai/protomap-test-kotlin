package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted appointment mapping records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a weighted appointment mapping is in its life cycle. */
enum class WeightedAppointmentMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted appointment mapping. Its code is unique within a tenant. */
data class WeightedAppointmentMapping(
    var code: String,
    var label: String,
    var approved: Boolean,
    var createdAt: Int,
    var minimum: Double,
    var lane: String,
    var title: String,
    var archived: Boolean,
    var region: String,
    var weight: Double,
    var status: WeightedAppointmentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAppointmentMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAppointmentMappingStatus): Boolean {
        when (status) {
            WeightedAppointmentMappingStatus.IDLE -> {
                when (next) {
                    WeightedAppointmentMappingStatus.WARMING, WeightedAppointmentMappingStatus.STOPPED -> return true
                }
                return false
            }
            WeightedAppointmentMappingStatus.WARMING -> return next == WeightedAppointmentMappingStatus.DRAINING
            WeightedAppointmentMappingStatus.READY -> return next == WeightedAppointmentMappingStatus.DRAINING
            WeightedAppointmentMappingStatus.DRAINING -> return next == WeightedAppointmentMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedAppointmentMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted appointment mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted appointment mapping"} $code"

    fun adjustedTotal(): Double = round(minimum * weight * 100) / 100

    fun snapshot(): WeightedAppointmentMapping {
        return WeightedAppointmentMapping(
            code = code,
            label = label,
            approved = approved,
            createdAt = createdAt,
            minimum = minimum,
            lane = lane,
            title = title,
            archived = archived,
            region = region,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted appointment mapping in status idle. */
fun newWeightedAppointmentMapping(code: String, label: String): WeightedAppointmentMapping {
    return WeightedAppointmentMapping(
        code = code,
        label = label,
        approved = false,
        createdAt = 0,
        minimum = 0.0,
        lane = "",
        title = "",
        archived = false,
        region = "",
        weight = 0.0,
        status = WeightedAppointmentMappingStatus.IDLE,
    )
}
