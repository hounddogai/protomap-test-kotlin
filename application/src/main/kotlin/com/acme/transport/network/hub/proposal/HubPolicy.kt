package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub policy is in its life cycle. */
enum class HubPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a hub policy and its current status. */
data class HubPolicy(
    var code: String,
    var label: String,
    var graceDays: Int,
    var floorValue: Double,
    var quantity: Int,
    var lane: String,
    var status: HubPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubPolicyStatus): Boolean {
        when (status) {
            HubPolicyStatus.IDLE -> return next == HubPolicyStatus.DRAINING
            HubPolicyStatus.WARMING -> {
                when (next) {
                    HubPolicyStatus.DRAINING -> return true
                }
                return false
            }
            HubPolicyStatus.READY -> return next == HubPolicyStatus.DRAINING || next == HubPolicyStatus.STOPPED
            HubPolicyStatus.DRAINING -> return next == HubPolicyStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: HubPolicyStatus) {
        if (!canMoveTo(next)) {
            error("hub policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"hub policy"} $code"

    fun adjustedValue(): Double = round(floorValue * graceDays.toDouble() * 100) / 100

    fun snapshot(): HubPolicy {
        return HubPolicy(
            code = code,
            label = label,
            graceDays = graceDays,
            floorValue = floorValue,
            quantity = quantity,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hub policy in status idle. */
fun newHubPolicy(code: String, label: String): HubPolicy {
    return HubPolicy(
        code = code,
        label = label,
        graceDays = 0,
        floorValue = 0.0,
        quantity = 0,
        lane = "",
        status = HubPolicyStatus.IDLE,
    )
}
