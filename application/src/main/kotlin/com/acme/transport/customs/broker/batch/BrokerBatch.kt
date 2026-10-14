package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker batch records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker batch is in its life cycle. */
enum class BrokerBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A broker batch. Its code is unique within a tenant. */
data class BrokerBatch(
    var code: String,
    var label: String,
    var pallets: Int,
    var stopCount: Int,
    var phone: String,
    var priority: Int,
    var currency: String,
    var active: Boolean,
    var segment: String,
    var depot: String,
    var status: BrokerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerBatchStatus): Boolean {
        when (status) {
            BrokerBatchStatus.REQUESTED -> {
                when (next) {
                    BrokerBatchStatus.REVOKED -> return true
                }
                return false
            }
            BrokerBatchStatus.GRANTED -> {
                when (next) {
                    BrokerBatchStatus.REVOKED -> return true
                }
                return false
            }
            BrokerBatchStatus.REVOKED -> return next == BrokerBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BrokerBatchStatus) {
        if (!canMoveTo(next)) {
            error("broker batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"broker batch"} $code"

    fun weightedVolume(): Double = round(pallets.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): BrokerBatch {
        return BrokerBatch(
            code = code,
            label = label,
            pallets = pallets,
            stopCount = stopCount,
            phone = phone,
            priority = priority,
            currency = currency,
            active = active,
            segment = segment,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a broker batch in status requested. */
fun newBrokerBatch(code: String, label: String): BrokerBatch {
    return BrokerBatch(
        code = code,
        label = label,
        pallets = 0,
        stopCount = 0,
        phone = "",
        priority = 0,
        currency = "",
        active = false,
        segment = "",
        depot = "",
        status = BrokerBatchStatus.REQUESTED,
    )
}
