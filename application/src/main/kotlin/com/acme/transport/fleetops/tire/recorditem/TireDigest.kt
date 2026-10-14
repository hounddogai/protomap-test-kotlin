package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire digest is in its life cycle. */
enum class TireDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a tire digest and its current status. */
data class TireDigest(
    var code: String,
    var name: String,
    var active: Boolean,
    var notes: String,
    var visible: Boolean,
    var archived: Boolean,
    var cost: Double,
    var floorValue: Double,
    var status: TireDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireDigestStatus): Boolean {
        when (status) {
            TireDigestStatus.PLANNED -> return next == TireDigestStatus.SCHEDULED || next == TireDigestStatus.IN_PROGRESS
            TireDigestStatus.SCHEDULED -> return next == TireDigestStatus.COMPLETED || next == TireDigestStatus.CANCELLED
            TireDigestStatus.IN_PROGRESS -> {
                when (next) {
                    TireDigestStatus.COMPLETED, TireDigestStatus.CANCELLED -> return true
                }
                return false
            }
            TireDigestStatus.COMPLETED -> {
                when (next) {
                    TireDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireDigestStatus) {
        if (!canMoveTo(next)) {
            error("tire digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire digest"} $code"

    fun adjustedVolume(): Double = round((floorValue + cost) * 100) / 100

    fun snapshot(): TireDigest {
        return TireDigest(
            code = code,
            name = name,
            active = active,
            notes = notes,
            visible = visible,
            archived = archived,
            cost = cost,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tire digest in status planned. */
fun newTireDigest(code: String, name: String): TireDigest {
    return TireDigest(
        code = code,
        name = name,
        active = false,
        notes = "",
        visible = false,
        archived = false,
        cost = 0.0,
        floorValue = 0.0,
        status = TireDigestStatus.PLANNED,
    )
}
