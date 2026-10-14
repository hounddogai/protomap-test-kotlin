package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery allocation is in its life cycle. */
enum class RedeliveryAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery allocation as the redelivery team keeps it. */
data class RedeliveryAllocation(
    var code: String,
    var name: String,
    var ownerId: String,
    var channel: String,
    var pinned: Boolean,
    var revision: Int,
    var locale: String,
    var pallets: Int,
    var graceDays: Int,
    var description: String,
    var status: RedeliveryAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryAllocationStatus): Boolean {
        when (status) {
            RedeliveryAllocationStatus.NEW -> return next == RedeliveryAllocationStatus.RUNNING || next == RedeliveryAllocationStatus.FAILED
            RedeliveryAllocationStatus.QUEUED -> {
                when (next) {
                    RedeliveryAllocationStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryAllocationStatus.RUNNING -> {
                when (next) {
                    RedeliveryAllocationStatus.DONE -> return true
                }
                return false
            }
            RedeliveryAllocationStatus.DONE -> {
                when (next) {
                    RedeliveryAllocationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryAllocationStatus) {
        if (!canMoveTo(next)) {
            error("redelivery allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((revision.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryAllocation {
        return RedeliveryAllocation(
            code = code,
            name = name,
            ownerId = ownerId,
            channel = channel,
            pinned = pinned,
            revision = revision,
            locale = locale,
            pallets = pallets,
            graceDays = graceDays,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a redelivery allocation in status new. */
fun newRedeliveryAllocation(code: String, name: String): RedeliveryAllocation {
    return RedeliveryAllocation(
        code = code,
        name = name,
        ownerId = "",
        channel = "",
        pinned = false,
        revision = 0,
        locale = "",
        pallets = 0,
        graceDays = 0,
        description = "",
        status = RedeliveryAllocationStatus.NEW,
    )
}
