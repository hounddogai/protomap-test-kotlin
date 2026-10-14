package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default fuel burn assignment is in its life cycle. */
enum class DefaultFuelBurnAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default fuel burn assignment. Its code is unique within a tenant. */
data class DefaultFuelBurnAssignment(
    var code: String,
    var title: String,
    var minimum: Double,
    var fuelLiters: Double,
    var lane: String,
    var attributes: MutableMap<String, String>,
    var externalRef: String,
    var threshold: Double,
    var windowDays: Int,
    var status: DefaultFuelBurnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFuelBurnAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFuelBurnAssignmentStatus): Boolean {
        when (status) {
            DefaultFuelBurnAssignmentStatus.DRAFT -> {
                when (next) {
                    DefaultFuelBurnAssignmentStatus.ACTIVE, DefaultFuelBurnAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultFuelBurnAssignmentStatus.PENDING -> return next == DefaultFuelBurnAssignmentStatus.SUSPENDED
            DefaultFuelBurnAssignmentStatus.ACTIVE -> {
                when (next) {
                    DefaultFuelBurnAssignmentStatus.SUSPENDED, DefaultFuelBurnAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            DefaultFuelBurnAssignmentStatus.SUSPENDED -> return next == DefaultFuelBurnAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultFuelBurnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default fuel burn assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default fuel burn assignment"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedVolume(): Double = round((minimum + fuelLiters) * 100) / 100

    fun snapshot(): DefaultFuelBurnAssignment {
        return DefaultFuelBurnAssignment(
            code = code,
            title = title,
            minimum = minimum,
            fuelLiters = fuelLiters,
            lane = lane,
            attributes = attributes,
            externalRef = externalRef,
            threshold = threshold,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default fuel burn assignment in status draft. */
fun newDefaultFuelBurnAssignment(code: String, title: String): DefaultFuelBurnAssignment {
    return DefaultFuelBurnAssignment(
        code = code,
        title = title,
        minimum = 0.0,
        fuelLiters = 0.0,
        lane = "",
        attributes = mutableMapOf<String, String>(),
        externalRef = "",
        threshold = 0.0,
        windowDays = 0,
        status = DefaultFuelBurnAssignmentStatus.DRAFT,
    )
}
