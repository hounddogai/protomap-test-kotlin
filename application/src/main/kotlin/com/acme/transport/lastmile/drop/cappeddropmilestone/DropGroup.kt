package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop group is in its life cycle. */
enum class DropGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A drop group. Its code is unique within a tenant. */
data class DropGroup(
    var code: String,
    var name: String,
    var timezone: String,
    var ownerId: String,
    var maximum: Double,
    var floorValue: Double,
    var carrier: String,
    var fuelLiters: Double,
    var batchSize: Int,
    var routeCode: String,
    var sequence: Int,
    var status: DropGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DropGroupStatus): Boolean {
        when (status) {
            DropGroupStatus.PROPOSED -> return next == DropGroupStatus.APPROVED
            DropGroupStatus.APPROVED -> return next == DropGroupStatus.REJECTED || next == DropGroupStatus.WITHDRAWN
            DropGroupStatus.REJECTED -> return next == DropGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DropGroupStatus) {
        if (!canMoveTo(next)) {
            error("drop group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round(sequence.toDouble() * maximum * 100) / 100

    fun snapshot(): DropGroup {
        return DropGroup(
            code = code,
            name = name,
            timezone = timezone,
            ownerId = ownerId,
            maximum = maximum,
            floorValue = floorValue,
            carrier = carrier,
            fuelLiters = fuelLiters,
            batchSize = batchSize,
            routeCode = routeCode,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a drop group in status proposed. */
fun newDropGroup(code: String, name: String): DropGroup {
    return DropGroup(
        code = code,
        name = name,
        timezone = "",
        ownerId = "",
        maximum = 0.0,
        floorValue = 0.0,
        carrier = "",
        fuelLiters = 0.0,
        batchSize = 0,
        routeCode = "",
        sequence = 0,
        status = DropGroupStatus.PROPOSED,
    )
}
