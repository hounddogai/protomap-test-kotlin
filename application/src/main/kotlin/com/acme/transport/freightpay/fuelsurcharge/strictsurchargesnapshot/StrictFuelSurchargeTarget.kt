package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict fuel surcharge target is in its life cycle. */
enum class StrictFuelSurchargeTargetStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict fuel surcharge target, with what the planners need to act on it. */
data class StrictFuelSurchargeTarget(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var weight: Double,
    var stopCount: Int,
    var status: StrictFuelSurchargeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFuelSurchargeTargetStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFuelSurchargeTargetStatus): Boolean {
        when (status) {
            StrictFuelSurchargeTargetStatus.DRAFT -> return next == StrictFuelSurchargeTargetStatus.PENDING
            StrictFuelSurchargeTargetStatus.PENDING -> return next == StrictFuelSurchargeTargetStatus.SUSPENDED || next == StrictFuelSurchargeTargetStatus.CLOSED
            StrictFuelSurchargeTargetStatus.ACTIVE -> return next == StrictFuelSurchargeTargetStatus.CLOSED
            StrictFuelSurchargeTargetStatus.SUSPENDED -> {
                when (next) {
                    StrictFuelSurchargeTargetStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictFuelSurchargeTargetStatus) {
        if (!canMoveTo(next)) {
            error("strict fuel surcharge target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict fuel surcharge target"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round(stopCount.toDouble() * weight * 100) / 100

    fun snapshot(): StrictFuelSurchargeTarget {
        return StrictFuelSurchargeTarget(
            code = code,
            name = name,
            tags = tags,
            weight = weight,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict fuel surcharge target in status draft. */
fun newStrictFuelSurchargeTarget(code: String, name: String): StrictFuelSurchargeTarget {
    return StrictFuelSurchargeTarget(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        weight = 0.0,
        stopCount = 0,
        status = StrictFuelSurchargeTargetStatus.DRAFT,
    )
}
