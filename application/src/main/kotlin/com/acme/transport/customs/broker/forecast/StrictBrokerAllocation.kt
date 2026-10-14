package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict broker allocation is in its life cycle. */
enum class StrictBrokerAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict broker allocation. Its code is unique within a tenant. */
data class StrictBrokerAllocation(
    var code: String,
    var title: String,
    var startsAt: Int,
    var version: Int,
    var amount: Double,
    var maximum: Double,
    var floorValue: Double,
    var pallets: Int,
    var margin: Double,
    var etaMinutes: Int,
    var pinned: Boolean,
    var status: StrictBrokerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBrokerAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBrokerAllocationStatus): Boolean {
        when (status) {
            StrictBrokerAllocationStatus.REQUESTED -> return next == StrictBrokerAllocationStatus.REVOKED
            StrictBrokerAllocationStatus.GRANTED -> {
                when (next) {
                    StrictBrokerAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            StrictBrokerAllocationStatus.REVOKED -> {
                when (next) {
                    StrictBrokerAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBrokerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("strict broker allocation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((margin + version.toDouble()) * 100) / 100

    fun snapshot(): StrictBrokerAllocation {
        return StrictBrokerAllocation(
            code = code,
            title = title,
            startsAt = startsAt,
            version = version,
            amount = amount,
            maximum = maximum,
            floorValue = floorValue,
            pallets = pallets,
            margin = margin,
            etaMinutes = etaMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict broker allocation in status requested. */
fun newStrictBrokerAllocation(code: String, title: String): StrictBrokerAllocation {
    return StrictBrokerAllocation(
        code = code,
        title = title,
        startsAt = 0,
        version = 0,
        amount = 0.0,
        maximum = 0.0,
        floorValue = 0.0,
        pallets = 0,
        margin = 0.0,
        etaMinutes = 0,
        pinned = false,
        status = StrictBrokerAllocationStatus.REQUESTED,
    )
}
