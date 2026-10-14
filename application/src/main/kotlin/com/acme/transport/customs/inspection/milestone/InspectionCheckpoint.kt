package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection checkpoint is in its life cycle. */
enum class InspectionCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection checkpoint. Its code is unique within a tenant. */
data class InspectionCheckpoint(
    var code: String,
    var name: String,
    var vehicleId: String,
    var createdAt: Int,
    var approved: Boolean,
    var shiftCode: String,
    var description: String,
    var startsAt: Int,
    var loadKg: Double,
    var routeCode: String,
    var status: InspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionCheckpointStatus): Boolean {
        when (status) {
            InspectionCheckpointStatus.DRAFT -> {
                when (next) {
                    InspectionCheckpointStatus.PENDING, InspectionCheckpointStatus.ACTIVE -> return true
                }
                return false
            }
            InspectionCheckpointStatus.PENDING -> {
                when (next) {
                    InspectionCheckpointStatus.ACTIVE, InspectionCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionCheckpointStatus.ACTIVE -> {
                when (next) {
                    InspectionCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionCheckpointStatus.SUSPENDED -> return next == InspectionCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"inspection checkpoint"} $code"

    fun netVolume(): Double = round((createdAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionCheckpoint {
        return InspectionCheckpoint(
            code = code,
            name = name,
            vehicleId = vehicleId,
            createdAt = createdAt,
            approved = approved,
            shiftCode = shiftCode,
            description = description,
            startsAt = startsAt,
            loadKg = loadKg,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection checkpoint in status draft. */
fun newInspectionCheckpoint(code: String, name: String): InspectionCheckpoint {
    return InspectionCheckpoint(
        code = code,
        name = name,
        vehicleId = "",
        createdAt = 0,
        approved = false,
        shiftCode = "",
        description = "",
        startsAt = 0,
        loadKg = 0.0,
        routeCode = "",
        status = InspectionCheckpointStatus.DRAFT,
    )
}
