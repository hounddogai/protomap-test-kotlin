package com.acme.transport.lastmile.parcel.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending parcel policy records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a pending parcel policy is in its life cycle. */
enum class PendingParcelPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending parcel policy, with what the planners need to act on it. */
data class PendingParcelPolicy(
    var code: String,
    var name: String,
    var approved: Boolean,
    var carrier: String,
    var sequence: Int,
    var threshold: Double,
    var attributes: MutableMap<String, String>,
    var fuelLiters: Double,
    var status: PendingParcelPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingParcelPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingParcelPolicyStatus): Boolean {
        when (status) {
            PendingParcelPolicyStatus.PLANNED -> return next == PendingParcelPolicyStatus.SCHEDULED
            PendingParcelPolicyStatus.SCHEDULED -> {
                when (next) {
                    PendingParcelPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingParcelPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    PendingParcelPolicyStatus.COMPLETED, PendingParcelPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            PendingParcelPolicyStatus.COMPLETED -> return next == PendingParcelPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingParcelPolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending parcel policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((sequence.toDouble() - threshold) * 100) / 100

    fun snapshot(): PendingParcelPolicy {
        return PendingParcelPolicy(
            code = code,
            name = name,
            approved = approved,
            carrier = carrier,
            sequence = sequence,
            threshold = threshold,
            attributes = attributes,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending parcel policy in status planned. */
fun newPendingParcelPolicy(code: String, name: String): PendingParcelPolicy {
    return PendingParcelPolicy(
        code = code,
        name = name,
        approved = false,
        carrier = "",
        sequence = 0,
        threshold = 0.0,
        attributes = mutableMapOf<String, String>(),
        fuelLiters = 0.0,
        status = PendingParcelPolicyStatus.PLANNED,
    )
}
