package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global node policy is in its life cycle. */
enum class GlobalNodePolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global node policy as the node team keeps it. */
data class GlobalNodePolicy(
    var code: String,
    var label: String,
    var discount: Double,
    var weight: Double,
    var windowDays: Int,
    var checksum: String,
    var shiftCode: String,
    var endsAt: Int,
    var maximum: Double,
    var channel: String,
    var trailerId: String,
    var status: GlobalNodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalNodePolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalNodePolicyStatus): Boolean {
        when (status) {
            GlobalNodePolicyStatus.PROPOSED -> {
                when (next) {
                    GlobalNodePolicyStatus.REJECTED, GlobalNodePolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalNodePolicyStatus.APPROVED -> return next == GlobalNodePolicyStatus.REJECTED
            GlobalNodePolicyStatus.REJECTED -> return next == GlobalNodePolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalNodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("global node policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((discount + windowDays.toDouble()) * 100) / 100

    fun snapshot(): GlobalNodePolicy {
        return GlobalNodePolicy(
            code = code,
            label = label,
            discount = discount,
            weight = weight,
            windowDays = windowDays,
            checksum = checksum,
            shiftCode = shiftCode,
            endsAt = endsAt,
            maximum = maximum,
            channel = channel,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global node policy in status proposed. */
fun newGlobalNodePolicy(code: String, label: String): GlobalNodePolicy {
    return GlobalNodePolicy(
        code = code,
        label = label,
        discount = 0.0,
        weight = 0.0,
        windowDays = 0,
        checksum = "",
        shiftCode = "",
        endsAt = 0,
        maximum = 0.0,
        channel = "",
        trailerId = "",
        status = GlobalNodePolicyStatus.PROPOSED,
    )
}
