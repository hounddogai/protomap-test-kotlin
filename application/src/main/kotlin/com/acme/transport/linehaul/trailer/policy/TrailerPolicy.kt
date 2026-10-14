package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer policy records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer policy is in its life cycle. */
enum class TrailerPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One trailer policy, with what the planners need to act on it. */
data class TrailerPolicy(
    var code: String,
    var label: String,
    var routeCode: String,
    var category: String,
    var dock: String,
    var driverId: String,
    var discount: Double,
    var odometer: Int,
    var revision: Int,
    var status: TrailerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerPolicyStatus): Boolean {
        when (status) {
            TrailerPolicyStatus.REQUESTED -> return next == TrailerPolicyStatus.REVOKED
            TrailerPolicyStatus.GRANTED -> return next == TrailerPolicyStatus.REVOKED || next == TrailerPolicyStatus.EXPIRED
            TrailerPolicyStatus.REVOKED -> {
                when (next) {
                    TrailerPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("trailer policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"trailer policy"} $code"

    fun effectiveVolume(): Double = round((discount + revision.toDouble()) * 100) / 100

    fun snapshot(): TrailerPolicy {
        return TrailerPolicy(
            code = code,
            label = label,
            routeCode = routeCode,
            category = category,
            dock = dock,
            driverId = driverId,
            discount = discount,
            odometer = odometer,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a trailer policy in status requested. */
fun newTrailerPolicy(code: String, label: String): TrailerPolicy {
    return TrailerPolicy(
        code = code,
        label = label,
        routeCode = "",
        category = "",
        dock = "",
        driverId = "",
        discount = 0.0,
        odometer = 0,
        revision = 0,
        status = TrailerPolicyStatus.REQUESTED,
    )
}
