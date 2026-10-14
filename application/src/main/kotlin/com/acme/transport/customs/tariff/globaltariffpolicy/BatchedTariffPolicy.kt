package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched tariff policy records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a batched tariff policy is in its life cycle. */
enum class BatchedTariffPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched tariff policy, with what the planners need to act on it. */
data class BatchedTariffPolicy(
    var code: String,
    var name: String,
    var externalRef: String,
    var vehicleId: String,
    var pallets: Int,
    var createdAt: Int,
    var retries: Int,
    var amount: Double,
    var status: BatchedTariffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffPolicyStatus): Boolean {
        when (status) {
            BatchedTariffPolicyStatus.IDLE -> return next == BatchedTariffPolicyStatus.WARMING || next == BatchedTariffPolicyStatus.READY
            BatchedTariffPolicyStatus.WARMING -> {
                when (next) {
                    BatchedTariffPolicyStatus.READY -> return true
                }
                return false
            }
            BatchedTariffPolicyStatus.READY -> {
                when (next) {
                    BatchedTariffPolicyStatus.DRAINING -> return true
                }
                return false
            }
            BatchedTariffPolicyStatus.DRAINING -> {
                when (next) {
                    BatchedTariffPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTariffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((retries.toDouble() + amount) * 100) / 100

    fun snapshot(): BatchedTariffPolicy {
        return BatchedTariffPolicy(
            code = code,
            name = name,
            externalRef = externalRef,
            vehicleId = vehicleId,
            pallets = pallets,
            createdAt = createdAt,
            retries = retries,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched tariff policy in status idle. */
fun newBatchedTariffPolicy(code: String, name: String): BatchedTariffPolicy {
    return BatchedTariffPolicy(
        code = code,
        name = name,
        externalRef = "",
        vehicleId = "",
        pallets = 0,
        createdAt = 0,
        retries = 0,
        amount = 0.0,
        status = BatchedTariffPolicyStatus.IDLE,
    )
}
