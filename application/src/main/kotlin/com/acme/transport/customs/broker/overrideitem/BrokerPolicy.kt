package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker policy is in its life cycle. */
enum class BrokerPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A broker policy as the broker team keeps it. */
data class BrokerPolicy(
    var code: String,
    var title: String,
    var carrier: String,
    var priority: Int,
    var stopCount: Int,
    var region: String,
    var revision: Int,
    var trailerId: String,
    var status: BrokerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerPolicyStatus): Boolean {
        when (status) {
            BrokerPolicyStatus.IDLE -> return next == BrokerPolicyStatus.READY || next == BrokerPolicyStatus.STOPPED
            BrokerPolicyStatus.WARMING -> return next == BrokerPolicyStatus.DRAINING
            BrokerPolicyStatus.READY -> return next == BrokerPolicyStatus.DRAINING
            BrokerPolicyStatus.DRAINING -> {
                when (next) {
                    BrokerPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("broker policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): BrokerPolicy {
        return BrokerPolicy(
            code = code,
            title = title,
            carrier = carrier,
            priority = priority,
            stopCount = stopCount,
            region = region,
            revision = revision,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a broker policy in status idle. */
fun newBrokerPolicy(code: String, title: String): BrokerPolicy {
    return BrokerPolicy(
        code = code,
        title = title,
        carrier = "",
        priority = 0,
        stopCount = 0,
        region = "",
        revision = 0,
        trailerId = "",
        status = BrokerPolicyStatus.IDLE,
    )
}
