package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted parcel target is in its life cycle. */
enum class WeightedParcelTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One weighted parcel target, with what the planners need to act on it. */
data class WeightedParcelTarget(
    var code: String,
    var name: String,
    var quantity: Int,
    var version: Int,
    var approved: Boolean,
    var region: String,
    var category: String,
    var etaMinutes: Int,
    var pallets: Int,
    var trailerId: String,
    var status: WeightedParcelTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedParcelTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedParcelTargetStatus): Boolean {
        when (status) {
            WeightedParcelTargetStatus.PROPOSED -> {
                when (next) {
                    WeightedParcelTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedParcelTargetStatus.APPROVED -> {
                when (next) {
                    WeightedParcelTargetStatus.REJECTED, WeightedParcelTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedParcelTargetStatus.REJECTED -> return next == WeightedParcelTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedParcelTargetStatus) {
        if (!canMoveTo(next)) {
            error("weighted parcel target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round(pallets.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): WeightedParcelTarget {
        return WeightedParcelTarget(
            code = code,
            name = name,
            quantity = quantity,
            version = version,
            approved = approved,
            region = region,
            category = category,
            etaMinutes = etaMinutes,
            pallets = pallets,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted parcel target in status proposed. */
fun newWeightedParcelTarget(code: String, name: String): WeightedParcelTarget {
    return WeightedParcelTarget(
        code = code,
        name = name,
        quantity = 0,
        version = 0,
        approved = false,
        region = "",
        category = "",
        etaMinutes = 0,
        pallets = 0,
        trailerId = "",
        status = WeightedParcelTargetStatus.PROPOSED,
    )
}
