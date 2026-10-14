package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched emission factor policy is in its life cycle. */
enum class BatchedEmissionFactorPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched emission factor policy and its current status. */
data class BatchedEmissionFactorPolicy(
    var code: String,
    var title: String,
    var archived: Boolean,
    var externalRef: String,
    var vehicleId: String,
    var currency: String,
    var loadKg: Double,
    var cost: Double,
    var score: Double,
    var etaMinutes: Int,
    var pinned: Boolean,
    var status: BatchedEmissionFactorPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorPolicyStatus): Boolean {
        when (status) {
            BatchedEmissionFactorPolicyStatus.PLANNED -> return next == BatchedEmissionFactorPolicyStatus.SCHEDULED || next == BatchedEmissionFactorPolicyStatus.IN_PROGRESS
            BatchedEmissionFactorPolicyStatus.SCHEDULED -> return next == BatchedEmissionFactorPolicyStatus.COMPLETED
            BatchedEmissionFactorPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedEmissionFactorPolicyStatus.COMPLETED, BatchedEmissionFactorPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedEmissionFactorPolicyStatus.COMPLETED -> return next == BatchedEmissionFactorPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((score + cost) * 100) / 100

    fun snapshot(): BatchedEmissionFactorPolicy {
        return BatchedEmissionFactorPolicy(
            code = code,
            title = title,
            archived = archived,
            externalRef = externalRef,
            vehicleId = vehicleId,
            currency = currency,
            loadKg = loadKg,
            cost = cost,
            score = score,
            etaMinutes = etaMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched emission factor policy in status planned. */
fun newBatchedEmissionFactorPolicy(code: String, title: String): BatchedEmissionFactorPolicy {
    return BatchedEmissionFactorPolicy(
        code = code,
        title = title,
        archived = false,
        externalRef = "",
        vehicleId = "",
        currency = "",
        loadKg = 0.0,
        cost = 0.0,
        score = 0.0,
        etaMinutes = 0,
        pinned = false,
        status = BatchedEmissionFactorPolicyStatus.PLANNED,
    )
}
