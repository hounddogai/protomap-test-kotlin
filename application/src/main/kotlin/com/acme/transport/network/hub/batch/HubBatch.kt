package com.acme.transport.network.hub.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub batch records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub batch is in its life cycle. */
enum class HubBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A hub batch as the hub team keeps it. */
data class HubBatch(
    var code: String,
    var name: String,
    var channel: String,
    var loadKg: Double,
    var vehicleId: String,
    var rate: Double,
    var trailerId: String,
    var carrier: String,
    var amount: Double,
    var retries: Int,
    var attempts: Int,
    var status: HubBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubBatchStatus): Boolean {
        when (status) {
            HubBatchStatus.DRAFT -> return next == HubBatchStatus.ACTIVE
            HubBatchStatus.PENDING -> return next == HubBatchStatus.ACTIVE || next == HubBatchStatus.SUSPENDED
            HubBatchStatus.ACTIVE -> {
                when (next) {
                    HubBatchStatus.SUSPENDED, HubBatchStatus.CLOSED -> return true
                }
                return false
            }
            HubBatchStatus.SUSPENDED -> {
                when (next) {
                    HubBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubBatchStatus) {
        if (!canMoveTo(next)) {
            error("hub batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round(attempts.toDouble() * rate * 100) / 100

    fun snapshot(): HubBatch {
        return HubBatch(
            code = code,
            name = name,
            channel = channel,
            loadKg = loadKg,
            vehicleId = vehicleId,
            rate = rate,
            trailerId = trailerId,
            carrier = carrier,
            amount = amount,
            retries = retries,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a hub batch in status draft. */
fun newHubBatch(code: String, name: String): HubBatch {
    return HubBatch(
        code = code,
        name = name,
        channel = "",
        loadKg = 0.0,
        vehicleId = "",
        rate = 0.0,
        trailerId = "",
        carrier = "",
        amount = 0.0,
        retries = 0,
        attempts = 0,
        status = HubBatchStatus.DRAFT,
    )
}
