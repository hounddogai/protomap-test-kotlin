package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster policy is in its life cycle. */
enum class RosterPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster policy. Its code is unique within a tenant. */
data class RosterPolicy(
    var code: String,
    var label: String,
    var rate: Double,
    var currency: String,
    var budget: Double,
    var attributes: MutableMap<String, String>,
    var retries: Int,
    var pallets: Int,
    var status: RosterPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterPolicyStatus): Boolean {
        when (status) {
            RosterPolicyStatus.NEW -> return next == RosterPolicyStatus.DONE
            RosterPolicyStatus.QUEUED -> {
                when (next) {
                    RosterPolicyStatus.DONE -> return true
                }
                return false
            }
            RosterPolicyStatus.RUNNING -> return next == RosterPolicyStatus.FAILED
            RosterPolicyStatus.DONE -> return next == RosterPolicyStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RosterPolicyStatus) {
        if (!canMoveTo(next)) {
            error("roster policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster policy"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedExposure(): Double = round(retries.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): RosterPolicy {
        return RosterPolicy(
            code = code,
            label = label,
            rate = rate,
            currency = currency,
            budget = budget,
            attributes = attributes,
            retries = retries,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a roster policy in status new. */
fun newRosterPolicy(code: String, label: String): RosterPolicy {
    return RosterPolicy(
        code = code,
        label = label,
        rate = 0.0,
        currency = "",
        budget = 0.0,
        attributes = mutableMapOf<String, String>(),
        retries = 0,
        pallets = 0,
        status = RosterPolicyStatus.NEW,
    )
}
