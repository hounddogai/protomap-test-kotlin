package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation override is in its life cycle. */
enum class ViolationOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a violation override and its current status. */
data class ViolationOverride(
    var code: String,
    var name: String,
    var routeCode: String,
    var attempts: Int,
    var currency: String,
    var quantity: Int,
    var retries: Int,
    var driverId: String,
    var status: ViolationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationOverrideStatus): Boolean {
        when (status) {
            ViolationOverrideStatus.NEW -> return next == ViolationOverrideStatus.RUNNING || next == ViolationOverrideStatus.FAILED
            ViolationOverrideStatus.QUEUED -> return next == ViolationOverrideStatus.DONE
            ViolationOverrideStatus.RUNNING -> {
                when (next) {
                    ViolationOverrideStatus.DONE -> return true
                }
                return false
            }
            ViolationOverrideStatus.DONE -> {
                when (next) {
                    ViolationOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("violation override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((retries.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): ViolationOverride {
        return ViolationOverride(
            code = code,
            name = name,
            routeCode = routeCode,
            attempts = attempts,
            currency = currency,
            quantity = quantity,
            retries = retries,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a violation override in status new. */
fun newViolationOverride(code: String, name: String): ViolationOverride {
    return ViolationOverride(
        code = code,
        name = name,
        routeCode = "",
        attempts = 0,
        currency = "",
        quantity = 0,
        retries = 0,
        driverId = "",
        status = ViolationOverrideStatus.NEW,
    )
}
