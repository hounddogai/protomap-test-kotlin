package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted tire milestone is in its life cycle. */
enum class WeightedTireMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted tire milestone. Its code is unique within a tenant. */
data class WeightedTireMilestone(
    var code: String,
    var name: String,
    var loadKg: Double,
    var currency: String,
    var revision: Int,
    var weight: Double,
    var description: String,
    var carrier: String,
    var contactEmail: String,
    var channel: String,
    var timezone: String,
    var status: WeightedTireMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTireMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTireMilestoneStatus): Boolean {
        when (status) {
            WeightedTireMilestoneStatus.PLANNED -> return next == WeightedTireMilestoneStatus.SCHEDULED || next == WeightedTireMilestoneStatus.IN_PROGRESS
            WeightedTireMilestoneStatus.SCHEDULED -> {
                when (next) {
                    WeightedTireMilestoneStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedTireMilestoneStatus.IN_PROGRESS -> return next == WeightedTireMilestoneStatus.COMPLETED || next == WeightedTireMilestoneStatus.CANCELLED
            WeightedTireMilestoneStatus.COMPLETED -> return next == WeightedTireMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedTireMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("weighted tire milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((revision.toDouble() + loadKg) * 100) / 100

    fun snapshot(): WeightedTireMilestone {
        return WeightedTireMilestone(
            code = code,
            name = name,
            loadKg = loadKg,
            currency = currency,
            revision = revision,
            weight = weight,
            description = description,
            carrier = carrier,
            contactEmail = contactEmail,
            channel = channel,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted tire milestone in status planned. */
fun newWeightedTireMilestone(code: String, name: String): WeightedTireMilestone {
    return WeightedTireMilestone(
        code = code,
        name = name,
        loadKg = 0.0,
        currency = "",
        revision = 0,
        weight = 0.0,
        description = "",
        carrier = "",
        contactEmail = "",
        channel = "",
        timezone = "",
        status = WeightedTireMilestoneStatus.PLANNED,
    )
}
