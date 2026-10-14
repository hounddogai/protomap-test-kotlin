package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional redelivery assignment is in its life cycle. */
enum class RegionalRedeliveryAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional redelivery assignment. Its code is unique within a tenant. */
data class RegionalRedeliveryAssignment(
    var code: String,
    var name: String,
    var windowDays: Int,
    var unit: String,
    var currency: String,
    var driverId: String,
    var minimum: Double,
    var carrier: String,
    var retries: Int,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var status: RegionalRedeliveryAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRedeliveryAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRedeliveryAssignmentStatus): Boolean {
        when (status) {
            RegionalRedeliveryAssignmentStatus.DRAFT -> {
                when (next) {
                    RegionalRedeliveryAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRedeliveryAssignmentStatus.PENDING -> {
                when (next) {
                    RegionalRedeliveryAssignmentStatus.ACTIVE, RegionalRedeliveryAssignmentStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalRedeliveryAssignmentStatus.ACTIVE -> {
                when (next) {
                    RegionalRedeliveryAssignmentStatus.SUSPENDED, RegionalRedeliveryAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            RegionalRedeliveryAssignmentStatus.SUSPENDED -> {
                when (next) {
                    RegionalRedeliveryAssignmentStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRedeliveryAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("regional redelivery assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional redelivery assignment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((fuelLiters - windowDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalRedeliveryAssignment {
        return RegionalRedeliveryAssignment(
            code = code,
            name = name,
            windowDays = windowDays,
            unit = unit,
            currency = currency,
            driverId = driverId,
            minimum = minimum,
            carrier = carrier,
            retries = retries,
            fuelLiters = fuelLiters,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional redelivery assignment in status draft. */
fun newRegionalRedeliveryAssignment(code: String, name: String): RegionalRedeliveryAssignment {
    return RegionalRedeliveryAssignment(
        code = code,
        name = name,
        windowDays = 0,
        unit = "",
        currency = "",
        driverId = "",
        minimum = 0.0,
        carrier = "",
        retries = 0,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        status = RegionalRedeliveryAssignmentStatus.DRAFT,
    )
}
