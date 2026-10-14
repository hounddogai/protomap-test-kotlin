package com.acme.transport.fleetops.leasereturn.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return target records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return target is in its life cycle. */
enum class LeaseReturnTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lease return target, with what the planners need to act on it. */
data class LeaseReturnTarget(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var batchSize: Int,
    var endsAt: Int,
    var phone: String,
    var pallets: Int,
    var carrier: String,
    var ceiling: Double,
    var label: String,
    var status: LeaseReturnTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnTargetStatus): Boolean {
        when (status) {
            LeaseReturnTargetStatus.PLANNED -> {
                when (next) {
                    LeaseReturnTargetStatus.IN_PROGRESS, LeaseReturnTargetStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnTargetStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnTargetStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnTargetStatus.IN_PROGRESS -> return next == LeaseReturnTargetStatus.COMPLETED || next == LeaseReturnTargetStatus.CANCELLED
            LeaseReturnTargetStatus.COMPLETED -> return next == LeaseReturnTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnTargetStatus) {
        if (!canMoveTo(next)) {
            error("lease return target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((expiresAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnTarget {
        return LeaseReturnTarget(
            code = code,
            title = title,
            expiresAt = expiresAt,
            batchSize = batchSize,
            endsAt = endsAt,
            phone = phone,
            pallets = pallets,
            carrier = carrier,
            ceiling = ceiling,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lease return target in status planned. */
fun newLeaseReturnTarget(code: String, title: String): LeaseReturnTarget {
    return LeaseReturnTarget(
        code = code,
        title = title,
        expiresAt = 0,
        batchSize = 0,
        endsAt = 0,
        phone = "",
        pallets = 0,
        carrier = "",
        ceiling = 0.0,
        label = "",
        status = LeaseReturnTargetStatus.PLANNED,
    )
}
