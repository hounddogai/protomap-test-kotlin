package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary cross dock policy records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a primary cross dock policy is in its life cycle. */
enum class PrimaryCrossDockPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary cross dock policy as the cross dock team keeps it. */
data class PrimaryCrossDockPolicy(
    var code: String,
    var label: String,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var priority: Int,
    var country: String,
    var ratio: Double,
    var minimum: Double,
    var channel: String,
    var name: String,
    var status: PrimaryCrossDockPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCrossDockPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCrossDockPolicyStatus): Boolean {
        when (status) {
            PrimaryCrossDockPolicyStatus.CREATED -> {
                when (next) {
                    PrimaryCrossDockPolicyStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryCrossDockPolicyStatus.VALIDATED -> return next == PrimaryCrossDockPolicyStatus.PUBLISHED || next == PrimaryCrossDockPolicyStatus.RETIRED
            PrimaryCrossDockPolicyStatus.PUBLISHED -> {
                when (next) {
                    PrimaryCrossDockPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCrossDockPolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary cross dock policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((priority.toDouble() - ratio) * 100) / 100

    fun snapshot(): PrimaryCrossDockPolicy {
        return PrimaryCrossDockPolicy(
            code = code,
            label = label,
            margin = margin,
            attributes = attributes,
            depot = depot,
            priority = priority,
            country = country,
            ratio = ratio,
            minimum = minimum,
            channel = channel,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary cross dock policy in status created. */
fun newPrimaryCrossDockPolicy(code: String, label: String): PrimaryCrossDockPolicy {
    return PrimaryCrossDockPolicy(
        code = code,
        label = label,
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        priority = 0,
        country = "",
        ratio = 0.0,
        minimum = 0.0,
        channel = "",
        name = "",
        status = PrimaryCrossDockPolicyStatus.CREATED,
    )
}
