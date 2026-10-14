package com.acme.transport.network.node.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default node assignment is in its life cycle. */
enum class DefaultNodeAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default node assignment as the node team keeps it. */
data class DefaultNodeAssignment(
    var code: String,
    var title: String,
    var vehicleId: String,
    var description: String,
    var active: Boolean,
    var currency: String,
    var source: String,
    var fuelLiters: Double,
    var margin: Double,
    var name: String,
    var status: DefaultNodeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNodeAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNodeAssignmentStatus): Boolean {
        when (status) {
            DefaultNodeAssignmentStatus.IDLE -> {
                when (next) {
                    DefaultNodeAssignmentStatus.DRAINING -> return true
                }
                return false
            }
            DefaultNodeAssignmentStatus.WARMING -> return next == DefaultNodeAssignmentStatus.DRAINING
            DefaultNodeAssignmentStatus.READY -> {
                when (next) {
                    DefaultNodeAssignmentStatus.DRAINING, DefaultNodeAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            DefaultNodeAssignmentStatus.DRAINING -> {
                when (next) {
                    DefaultNodeAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultNodeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("default node assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round(margin * fuelLiters * 100) / 100

    fun snapshot(): DefaultNodeAssignment {
        return DefaultNodeAssignment(
            code = code,
            title = title,
            vehicleId = vehicleId,
            description = description,
            active = active,
            currency = currency,
            source = source,
            fuelLiters = fuelLiters,
            margin = margin,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default node assignment in status idle. */
fun newDefaultNodeAssignment(code: String, title: String): DefaultNodeAssignment {
    return DefaultNodeAssignment(
        code = code,
        title = title,
        vehicleId = "",
        description = "",
        active = false,
        currency = "",
        source = "",
        fuelLiters = 0.0,
        margin = 0.0,
        name = "",
        status = DefaultNodeAssignmentStatus.IDLE,
    )
}
