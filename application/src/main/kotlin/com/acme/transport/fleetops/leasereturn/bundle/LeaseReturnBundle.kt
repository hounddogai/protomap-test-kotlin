package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return bundle records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return bundle is in its life cycle. */
enum class LeaseReturnBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return bundle, with what the planners need to act on it. */
data class LeaseReturnBundle(
    var code: String,
    var name: String,
    var phone: String,
    var retries: Int,
    var startsAt: Int,
    var status: LeaseReturnBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnBundleStatus): Boolean {
        when (status) {
            LeaseReturnBundleStatus.PLANNED -> {
                when (next) {
                    LeaseReturnBundleStatus.SCHEDULED -> return true
                }
                return false
            }
            LeaseReturnBundleStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnBundleStatus.IN_PROGRESS, LeaseReturnBundleStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnBundleStatus.IN_PROGRESS -> return next == LeaseReturnBundleStatus.COMPLETED || next == LeaseReturnBundleStatus.CANCELLED
            LeaseReturnBundleStatus.COMPLETED -> {
                when (next) {
                    LeaseReturnBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnBundleStatus) {
        if (!canMoveTo(next)) {
            error("lease return bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((startsAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnBundle {
        return LeaseReturnBundle(
            code = code,
            name = name,
            phone = phone,
            retries = retries,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lease return bundle in status planned. */
fun newLeaseReturnBundle(code: String, name: String): LeaseReturnBundle {
    return LeaseReturnBundle(
        code = code,
        name = name,
        phone = "",
        retries = 0,
        startsAt = 0,
        status = LeaseReturnBundleStatus.PLANNED,
    )
}
