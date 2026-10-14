package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity policy is in its life cycle. */
enum class CapacityPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capacity policy as the capacity team keeps it. */
data class CapacityPolicy(
    var code: String,
    var name: String,
    var channel: String,
    var endsAt: Int,
    var pallets: Int,
    var batchSize: Int,
    var priority: Int,
    var floorValue: Double,
    var status: CapacityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityPolicyStatus): Boolean {
        when (status) {
            CapacityPolicyStatus.REQUESTED -> return next == CapacityPolicyStatus.GRANTED || next == CapacityPolicyStatus.REVOKED
            CapacityPolicyStatus.GRANTED -> {
                when (next) {
                    CapacityPolicyStatus.REVOKED, CapacityPolicyStatus.EXPIRED -> return true
                }
                return false
            }
            CapacityPolicyStatus.REVOKED -> {
                when (next) {
                    CapacityPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capacity policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netExposure(): Double = round((pallets.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): CapacityPolicy {
        return CapacityPolicy(
            code = code,
            name = name,
            channel = channel,
            endsAt = endsAt,
            pallets = pallets,
            batchSize = batchSize,
            priority = priority,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capacity policy in status requested. */
fun newCapacityPolicy(code: String, name: String): CapacityPolicy {
    return CapacityPolicy(
        code = code,
        name = name,
        channel = "",
        endsAt = 0,
        pallets = 0,
        batchSize = 0,
        priority = 0,
        floorValue = 0.0,
        status = CapacityPolicyStatus.REQUESTED,
    )
}
