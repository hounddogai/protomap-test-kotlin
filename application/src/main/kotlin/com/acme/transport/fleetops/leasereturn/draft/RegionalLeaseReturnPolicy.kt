package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize

/** Where a regional lease return policy is in its life cycle. */
enum class RegionalLeaseReturnPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional lease return policy, with what the planners need to act on it. */
data class RegionalLeaseReturnPolicy(
    var code: String,
    var label: String,
    var source: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var currency: String,
    var approved: Boolean,
    var score: Double,
    var status: RegionalLeaseReturnPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnPolicyStatus): Boolean {
        when (status) {
            RegionalLeaseReturnPolicyStatus.NEW -> {
                when (next) {
                    RegionalLeaseReturnPolicyStatus.QUEUED, RegionalLeaseReturnPolicyStatus.RUNNING -> return true
                }
                return false
            }
            RegionalLeaseReturnPolicyStatus.QUEUED -> return next == RegionalLeaseReturnPolicyStatus.RUNNING || next == RegionalLeaseReturnPolicyStatus.DONE
            RegionalLeaseReturnPolicyStatus.RUNNING -> {
                when (next) {
                    RegionalLeaseReturnPolicyStatus.DONE -> return true
                }
                return false
            }
            RegionalLeaseReturnPolicyStatus.DONE -> {
                when (next) {
                    RegionalLeaseReturnPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnPolicyStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): RegionalLeaseReturnPolicy {
        return RegionalLeaseReturnPolicy(
            code = code,
            label = label,
            source = source,
            attributes = attributes,
            active = active,
            currency = currency,
            approved = approved,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional lease return policy in status new. */
fun newRegionalLeaseReturnPolicy(code: String, label: String): RegionalLeaseReturnPolicy {
    return RegionalLeaseReturnPolicy(
        code = code,
        label = label,
        source = "",
        attributes = mutableMapOf<String, String>(),
        active = false,
        currency = "",
        approved = false,
        score = 0.0,
        status = RegionalLeaseReturnPolicyStatus.NEW,
    )
}
