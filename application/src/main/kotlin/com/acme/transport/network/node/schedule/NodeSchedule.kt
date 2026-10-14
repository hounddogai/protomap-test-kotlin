package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node schedule records of network's node work, and the rules, stores, and calculations around them.

/** Where a node schedule is in its life cycle. */
enum class NodeScheduleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node schedule. Its code is unique within a tenant. */
data class NodeSchedule(
    var code: String,
    var label: String,
    var discount: Double,
    var limit: Int,
    var sequence: Int,
    var fuelLiters: Double,
    var rate: Double,
    var etaMinutes: Int,
    var expiresAt: Int,
    var odometer: Int,
    var depot: String,
    var status: NodeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeScheduleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeScheduleStatus): Boolean {
        when (status) {
            NodeScheduleStatus.PLANNED -> return next == NodeScheduleStatus.IN_PROGRESS
            NodeScheduleStatus.SCHEDULED -> {
                when (next) {
                    NodeScheduleStatus.IN_PROGRESS -> return true
                }
                return false
            }
            NodeScheduleStatus.IN_PROGRESS -> {
                when (next) {
                    NodeScheduleStatus.COMPLETED, NodeScheduleStatus.CANCELLED -> return true
                }
                return false
            }
            NodeScheduleStatus.COMPLETED -> {
                when (next) {
                    NodeScheduleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("node schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round(sequence.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): NodeSchedule {
        return NodeSchedule(
            code = code,
            label = label,
            discount = discount,
            limit = limit,
            sequence = sequence,
            fuelLiters = fuelLiters,
            rate = rate,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            odometer = odometer,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a node schedule in status planned. */
fun newNodeSchedule(code: String, label: String): NodeSchedule {
    return NodeSchedule(
        code = code,
        label = label,
        discount = 0.0,
        limit = 0,
        sequence = 0,
        fuelLiters = 0.0,
        rate = 0.0,
        etaMinutes = 0,
        expiresAt = 0,
        odometer = 0,
        depot = "",
        status = NodeScheduleStatus.PLANNED,
    )
}
