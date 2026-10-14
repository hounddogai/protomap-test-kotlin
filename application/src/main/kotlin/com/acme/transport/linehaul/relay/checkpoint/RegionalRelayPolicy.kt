package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a regional relay policy is in its life cycle. */
enum class RegionalRelayPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional relay policy and its current status. */
data class RegionalRelayPolicy(
    var code: String,
    var name: String,
    var routeCode: String,
    var score: Double,
    var driverId: String,
    var status: RegionalRelayPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRelayPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRelayPolicyStatus): Boolean {
        when (status) {
            RegionalRelayPolicyStatus.IDLE -> {
                when (next) {
                    RegionalRelayPolicyStatus.WARMING -> return true
                }
                return false
            }
            RegionalRelayPolicyStatus.WARMING -> return next == RegionalRelayPolicyStatus.READY || next == RegionalRelayPolicyStatus.DRAINING
            RegionalRelayPolicyStatus.READY -> return next == RegionalRelayPolicyStatus.DRAINING || next == RegionalRelayPolicyStatus.STOPPED
            RegionalRelayPolicyStatus.DRAINING -> {
                when (next) {
                    RegionalRelayPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRelayPolicyStatus) {
        if (!canMoveTo(next)) {
            error("regional relay policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = score

    fun snapshot(): RegionalRelayPolicy {
        return RegionalRelayPolicy(
            code = code,
            name = name,
            routeCode = routeCode,
            score = score,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional relay policy in status idle. */
fun newRegionalRelayPolicy(code: String, name: String): RegionalRelayPolicy {
    return RegionalRelayPolicy(
        code = code,
        name = name,
        routeCode = "",
        score = 0.0,
        driverId = "",
        status = RegionalRelayPolicyStatus.IDLE,
    )
}
