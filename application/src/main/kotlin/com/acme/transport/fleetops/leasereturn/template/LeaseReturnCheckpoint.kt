package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return checkpoint is in its life cycle. */
enum class LeaseReturnCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lease return checkpoint. Its code is unique within a tenant. */
data class LeaseReturnCheckpoint(
    var code: String,
    var title: String,
    var maximum: Double,
    var threshold: Double,
    var attempts: Int,
    var country: String,
    var discount: Double,
    var carrier: String,
    var quantity: Int,
    var priority: Int,
    var status: LeaseReturnCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnCheckpointStatus): Boolean {
        when (status) {
            LeaseReturnCheckpointStatus.NEW -> return next == LeaseReturnCheckpointStatus.QUEUED || next == LeaseReturnCheckpointStatus.FAILED
            LeaseReturnCheckpointStatus.QUEUED -> return next == LeaseReturnCheckpointStatus.FAILED
            LeaseReturnCheckpointStatus.RUNNING -> {
                when (next) {
                    LeaseReturnCheckpointStatus.DONE -> return true
                }
                return false
            }
            LeaseReturnCheckpointStatus.DONE -> return next == LeaseReturnCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LeaseReturnCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lease return checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((priority.toDouble() + threshold) * 100) / 100

    fun snapshot(): LeaseReturnCheckpoint {
        return LeaseReturnCheckpoint(
            code = code,
            title = title,
            maximum = maximum,
            threshold = threshold,
            attempts = attempts,
            country = country,
            discount = discount,
            carrier = carrier,
            quantity = quantity,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lease return checkpoint in status new. */
fun newLeaseReturnCheckpoint(code: String, title: String): LeaseReturnCheckpoint {
    return LeaseReturnCheckpoint(
        code = code,
        title = title,
        maximum = 0.0,
        threshold = 0.0,
        attempts = 0,
        country = "",
        discount = 0.0,
        carrier = "",
        quantity = 0,
        priority = 0,
        status = LeaseReturnCheckpointStatus.NEW,
    )
}
