package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling accessorial policy is in its life cycle. */
enum class RollingAccessorialPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling accessorial policy. Its code is unique within a tenant. */
data class RollingAccessorialPolicy(
    var code: String,
    var label: String,
    var description: String,
    var batchSize: Int,
    var summary: String,
    var routeCode: String,
    var segment: String,
    var capacity: Int,
    var contactEmail: String,
    var active: Boolean,
    var driverId: String,
    var status: RollingAccessorialPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAccessorialPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAccessorialPolicyStatus): Boolean {
        when (status) {
            RollingAccessorialPolicyStatus.IDLE -> {
                when (next) {
                    RollingAccessorialPolicyStatus.WARMING, RollingAccessorialPolicyStatus.DRAINING -> return true
                }
                return false
            }
            RollingAccessorialPolicyStatus.WARMING -> return next == RollingAccessorialPolicyStatus.DRAINING
            RollingAccessorialPolicyStatus.READY -> return next == RollingAccessorialPolicyStatus.STOPPED
            RollingAccessorialPolicyStatus.DRAINING -> {
                when (next) {
                    RollingAccessorialPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAccessorialPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling accessorial policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round(batchSize.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): RollingAccessorialPolicy {
        return RollingAccessorialPolicy(
            code = code,
            label = label,
            description = description,
            batchSize = batchSize,
            summary = summary,
            routeCode = routeCode,
            segment = segment,
            capacity = capacity,
            contactEmail = contactEmail,
            active = active,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a rolling accessorial policy in status idle. */
fun newRollingAccessorialPolicy(code: String, label: String): RollingAccessorialPolicy {
    return RollingAccessorialPolicy(
        code = code,
        label = label,
        description = "",
        batchSize = 0,
        summary = "",
        routeCode = "",
        segment = "",
        capacity = 0,
        contactEmail = "",
        active = false,
        driverId = "",
        status = RollingAccessorialPolicyStatus.IDLE,
    )
}
