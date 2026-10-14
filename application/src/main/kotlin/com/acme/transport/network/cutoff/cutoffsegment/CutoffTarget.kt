package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff target is in its life cycle. */
enum class CutoffTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A cutoff target as the cutoff team keeps it. */
data class CutoffTarget(
    var code: String,
    var name: String,
    var limit: Int,
    var weight: Double,
    var currency: String,
    var fuelLiters: Double,
    var active: Boolean,
    var floorValue: Double,
    var routeCode: String,
    var status: CutoffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffTargetStatus): Boolean {
        when (status) {
            CutoffTargetStatus.PLANNED -> return next == CutoffTargetStatus.IN_PROGRESS || next == CutoffTargetStatus.CANCELLED
            CutoffTargetStatus.SCHEDULED -> {
                when (next) {
                    CutoffTargetStatus.CANCELLED -> return true
                }
                return false
            }
            CutoffTargetStatus.IN_PROGRESS -> return next == CutoffTargetStatus.COMPLETED || next == CutoffTargetStatus.CANCELLED
            CutoffTargetStatus.COMPLETED -> return next == CutoffTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CutoffTargetStatus) {
        if (!canMoveTo(next)) {
            error("cutoff target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netVolume(): Double = round((weight + floorValue) * 100) / 100

    fun snapshot(): CutoffTarget {
        return CutoffTarget(
            code = code,
            name = name,
            limit = limit,
            weight = weight,
            currency = currency,
            fuelLiters = fuelLiters,
            active = active,
            floorValue = floorValue,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a cutoff target in status planned. */
fun newCutoffTarget(code: String, name: String): CutoffTarget {
    return CutoffTarget(
        code = code,
        name = name,
        limit = 0,
        weight = 0.0,
        currency = "",
        fuelLiters = 0.0,
        active = false,
        floorValue = 0.0,
        routeCode = "",
        status = CutoffTargetStatus.PLANNED,
    )
}
