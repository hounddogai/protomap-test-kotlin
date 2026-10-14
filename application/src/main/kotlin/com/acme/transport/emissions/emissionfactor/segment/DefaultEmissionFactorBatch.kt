package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default emission factor batch is in its life cycle. */
enum class DefaultEmissionFactorBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default emission factor batch as the emission factor team keeps it. */
data class DefaultEmissionFactorBatch(
    var code: String,
    var label: String,
    var retries: Int,
    var depot: String,
    var currency: String,
    var fuelLiters: Double,
    var score: Double,
    var notes: String,
    var summary: String,
    var status: DefaultEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEmissionFactorBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEmissionFactorBatchStatus): Boolean {
        when (status) {
            DefaultEmissionFactorBatchStatus.OPEN -> {
                when (next) {
                    DefaultEmissionFactorBatchStatus.RESOLVED -> return true
                }
                return false
            }
            DefaultEmissionFactorBatchStatus.IN_REVIEW -> {
                when (next) {
                    DefaultEmissionFactorBatchStatus.REOPENED -> return true
                }
                return false
            }
            DefaultEmissionFactorBatchStatus.RESOLVED -> {
                when (next) {
                    DefaultEmissionFactorBatchStatus.REOPENED -> return true
                }
                return false
            }
            DefaultEmissionFactorBatchStatus.REOPENED -> return next == DefaultEmissionFactorBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("default emission factor batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(fuelLiters * score * 100) / 100

    fun snapshot(): DefaultEmissionFactorBatch {
        return DefaultEmissionFactorBatch(
            code = code,
            label = label,
            retries = retries,
            depot = depot,
            currency = currency,
            fuelLiters = fuelLiters,
            score = score,
            notes = notes,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default emission factor batch in status open. */
fun newDefaultEmissionFactorBatch(code: String, label: String): DefaultEmissionFactorBatch {
    return DefaultEmissionFactorBatch(
        code = code,
        label = label,
        retries = 0,
        depot = "",
        currency = "",
        fuelLiters = 0.0,
        score = 0.0,
        notes = "",
        summary = "",
        status = DefaultEmissionFactorBatchStatus.OPEN,
    )
}
