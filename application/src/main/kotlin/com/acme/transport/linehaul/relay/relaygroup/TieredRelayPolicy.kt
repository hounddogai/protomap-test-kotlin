package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.text.normalize

/** Where a tiered relay policy is in its life cycle. */
enum class TieredRelayPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tiered relay policy. Its code is unique within a tenant. */
data class TieredRelayPolicy(
    var code: String,
    var label: String,
    var lane: String,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var status: TieredRelayPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRelayPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRelayPolicyStatus): Boolean {
        when (status) {
            TieredRelayPolicyStatus.OPEN -> return next == TieredRelayPolicyStatus.IN_REVIEW
            TieredRelayPolicyStatus.IN_REVIEW -> return next == TieredRelayPolicyStatus.RESOLVED || next == TieredRelayPolicyStatus.REOPENED
            TieredRelayPolicyStatus.RESOLVED -> return next == TieredRelayPolicyStatus.REOPENED
            TieredRelayPolicyStatus.REOPENED -> return next == TieredRelayPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TieredRelayPolicyStatus) {
        if (!canMoveTo(next)) {
            error("tiered relay policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tiered relay policy"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): TieredRelayPolicy {
        return TieredRelayPolicy(
            code = code,
            label = label,
            lane = lane,
            attributes = attributes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered relay policy in status open. */
fun newTieredRelayPolicy(code: String, label: String): TieredRelayPolicy {
    return TieredRelayPolicy(
        code = code,
        label = label,
        lane = "",
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        status = TieredRelayPolicyStatus.OPEN,
    )
}
