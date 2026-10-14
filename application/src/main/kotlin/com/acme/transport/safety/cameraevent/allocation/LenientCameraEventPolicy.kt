package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient camera event policy is in its life cycle. */
enum class LenientCameraEventPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient camera event policy, with what the planners need to act on it. */
data class LenientCameraEventPolicy(
    var code: String,
    var name: String,
    var margin: Double,
    var channel: String,
    var reviewer: String,
    var vehicleId: String,
    var phone: String,
    var ceiling: Double,
    var status: LenientCameraEventPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCameraEventPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCameraEventPolicyStatus): Boolean {
        when (status) {
            LenientCameraEventPolicyStatus.CREATED -> return next == LenientCameraEventPolicyStatus.VALIDATED
            LenientCameraEventPolicyStatus.VALIDATED -> {
                when (next) {
                    LenientCameraEventPolicyStatus.PUBLISHED, LenientCameraEventPolicyStatus.RETIRED -> return true
                }
                return false
            }
            LenientCameraEventPolicyStatus.PUBLISHED -> return next == LenientCameraEventPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientCameraEventPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient camera event policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((ceiling - margin) * 100) / 100

    fun snapshot(): LenientCameraEventPolicy {
        return LenientCameraEventPolicy(
            code = code,
            name = name,
            margin = margin,
            channel = channel,
            reviewer = reviewer,
            vehicleId = vehicleId,
            phone = phone,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lenient camera event policy in status created. */
fun newLenientCameraEventPolicy(code: String, name: String): LenientCameraEventPolicy {
    return LenientCameraEventPolicy(
        code = code,
        name = name,
        margin = 0.0,
        channel = "",
        reviewer = "",
        vehicleId = "",
        phone = "",
        ceiling = 0.0,
        status = LenientCameraEventPolicyStatus.CREATED,
    )
}
