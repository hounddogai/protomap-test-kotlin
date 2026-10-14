package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery assignment is in its life cycle. */
enum class SeasonalRedeliveryAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal redelivery assignment and its current status. */
data class SeasonalRedeliveryAssignment(
    var code: String,
    var name: String,
    var pallets: Int,
    var version: Int,
    var fuelLiters: Double,
    var weight: Double,
    var segment: String,
    var active: Boolean,
    var margin: Double,
    var archived: Boolean,
    var carrier: String,
    var status: SeasonalRedeliveryAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryAssignmentStatus): Boolean {
        when (status) {
            SeasonalRedeliveryAssignmentStatus.CREATED -> {
                when (next) {
                    SeasonalRedeliveryAssignmentStatus.PUBLISHED, SeasonalRedeliveryAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalRedeliveryAssignmentStatus.VALIDATED -> {
                when (next) {
                    SeasonalRedeliveryAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalRedeliveryAssignmentStatus.PUBLISHED -> return next == SeasonalRedeliveryAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery assignment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((version.toDouble() - weight) * 100) / 100

    fun snapshot(): SeasonalRedeliveryAssignment {
        return SeasonalRedeliveryAssignment(
            code = code,
            name = name,
            pallets = pallets,
            version = version,
            fuelLiters = fuelLiters,
            weight = weight,
            segment = segment,
            active = active,
            margin = margin,
            archived = archived,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal redelivery assignment in status created. */
fun newSeasonalRedeliveryAssignment(code: String, name: String): SeasonalRedeliveryAssignment {
    return SeasonalRedeliveryAssignment(
        code = code,
        name = name,
        pallets = 0,
        version = 0,
        fuelLiters = 0.0,
        weight = 0.0,
        segment = "",
        active = false,
        margin = 0.0,
        archived = false,
        carrier = "",
        status = SeasonalRedeliveryAssignmentStatus.CREATED,
    )
}
