package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.normalize

// The pending lease return preference records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a pending lease return preference is in its life cycle. */
enum class PendingLeaseReturnPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending lease return preference and its current status. */
data class PendingLeaseReturnPreference(
    var code: String,
    var title: String,
    var depot: String,
    var budget: Double,
    var reviewer: String,
    var attributes: MutableMap<String, String>,
    var status: PendingLeaseReturnPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLeaseReturnPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLeaseReturnPreferenceStatus): Boolean {
        when (status) {
            PendingLeaseReturnPreferenceStatus.PLANNED -> {
                when (next) {
                    PendingLeaseReturnPreferenceStatus.COMPLETED, PendingLeaseReturnPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            PendingLeaseReturnPreferenceStatus.SCHEDULED -> {
                when (next) {
                    PendingLeaseReturnPreferenceStatus.COMPLETED, PendingLeaseReturnPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            PendingLeaseReturnPreferenceStatus.IN_PROGRESS -> return next == PendingLeaseReturnPreferenceStatus.COMPLETED || next == PendingLeaseReturnPreferenceStatus.CANCELLED
            PendingLeaseReturnPreferenceStatus.COMPLETED -> return next == PendingLeaseReturnPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingLeaseReturnPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("pending lease return preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = budget

    fun snapshot(): PendingLeaseReturnPreference {
        return PendingLeaseReturnPreference(
            code = code,
            title = title,
            depot = depot,
            budget = budget,
            reviewer = reviewer,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a pending lease return preference in status planned. */
fun newPendingLeaseReturnPreference(code: String, title: String): PendingLeaseReturnPreference {
    return PendingLeaseReturnPreference(
        code = code,
        title = title,
        depot = "",
        budget = 0.0,
        reviewer = "",
        attributes = mutableMapOf<String, String>(),
        status = PendingLeaseReturnPreferenceStatus.PLANNED,
    )
}
