package com.acme.transport.linehaul.capacity.capacitymilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped capacity policy is in its life cycle. */
enum class CappedCapacityPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped capacity policy as the capacity team keeps it. */
data class CappedCapacityPolicy(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var rate: Double,
    var currency: String,
    var pinned: Boolean,
    var visible: Boolean,
    var status: CappedCapacityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCapacityPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCapacityPolicyStatus): Boolean {
        when (status) {
            CappedCapacityPolicyStatus.OPEN -> {
                when (next) {
                    CappedCapacityPolicyStatus.REOPENED, CappedCapacityPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedCapacityPolicyStatus.IN_REVIEW -> return next == CappedCapacityPolicyStatus.REOPENED || next == CappedCapacityPolicyStatus.ARCHIVED
            CappedCapacityPolicyStatus.RESOLVED -> {
                when (next) {
                    CappedCapacityPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedCapacityPolicyStatus.REOPENED -> return next == CappedCapacityPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedCapacityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped capacity policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round(distanceKm * rate * 100) / 100

    fun snapshot(): CappedCapacityPolicy {
        return CappedCapacityPolicy(
            code = code,
            name = name,
            distanceKm = distanceKm,
            rate = rate,
            currency = currency,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped capacity policy in status open. */
fun newCappedCapacityPolicy(code: String, name: String): CappedCapacityPolicy {
    return CappedCapacityPolicy(
        code = code,
        name = name,
        distanceKm = 0.0,
        rate = 0.0,
        currency = "",
        pinned = false,
        visible = false,
        status = CappedCapacityPolicyStatus.OPEN,
    )
}
