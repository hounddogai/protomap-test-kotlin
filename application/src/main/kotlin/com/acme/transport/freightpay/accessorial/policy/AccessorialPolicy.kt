package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial policy records of freightpay's accessorial work, and the rules, stores, and calculations
// around them.

/** Where a accessorial policy is in its life cycle. */
enum class AccessorialPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a accessorial policy and its current status. */
data class AccessorialPolicy(
    var code: String,
    var label: String,
    var segment: String,
    var limit: Int,
    var odometer: Int,
    var visible: Boolean,
    var fuelLiters: Double,
    var region: String,
    var threshold: Double,
    var stopCount: Int,
    var approved: Boolean,
    var status: AccessorialPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialPolicyStatus): Boolean {
        when (status) {
            AccessorialPolicyStatus.REQUESTED -> {
                when (next) {
                    AccessorialPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            AccessorialPolicyStatus.GRANTED -> return next == AccessorialPolicyStatus.EXPIRED
            AccessorialPolicyStatus.REVOKED -> return next == AccessorialPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialPolicyStatus) {
        if (!canMoveTo(next)) {
            error("accessorial policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netValue(): Double = round((limit.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): AccessorialPolicy {
        return AccessorialPolicy(
            code = code,
            label = label,
            segment = segment,
            limit = limit,
            odometer = odometer,
            visible = visible,
            fuelLiters = fuelLiters,
            region = region,
            threshold = threshold,
            stopCount = stopCount,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a accessorial policy in status requested. */
fun newAccessorialPolicy(code: String, label: String): AccessorialPolicy {
    return AccessorialPolicy(
        code = code,
        label = label,
        segment = "",
        limit = 0,
        odometer = 0,
        visible = false,
        fuelLiters = 0.0,
        region = "",
        threshold = 0.0,
        stopCount = 0,
        approved = false,
        status = AccessorialPolicyStatus.REQUESTED,
    )
}
