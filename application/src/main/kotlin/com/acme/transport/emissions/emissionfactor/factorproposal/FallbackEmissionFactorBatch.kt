package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback emission factor batch is in its life cycle. */
enum class FallbackEmissionFactorBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A fallback emission factor batch. Its code is unique within a tenant. */
data class FallbackEmissionFactorBatch(
    var code: String,
    var name: String,
    var endsAt: Int,
    var contactEmail: String,
    var ceiling: Double,
    var status: FallbackEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEmissionFactorBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEmissionFactorBatchStatus): Boolean {
        when (status) {
            FallbackEmissionFactorBatchStatus.DRAFT -> {
                when (next) {
                    FallbackEmissionFactorBatchStatus.SUSPENDED, FallbackEmissionFactorBatchStatus.CLOSED -> return true
                }
                return false
            }
            FallbackEmissionFactorBatchStatus.PENDING -> {
                when (next) {
                    FallbackEmissionFactorBatchStatus.ACTIVE -> return true
                }
                return false
            }
            FallbackEmissionFactorBatchStatus.ACTIVE -> {
                when (next) {
                    FallbackEmissionFactorBatchStatus.SUSPENDED, FallbackEmissionFactorBatchStatus.CLOSED -> return true
                }
                return false
            }
            FallbackEmissionFactorBatchStatus.SUSPENDED -> {
                when (next) {
                    FallbackEmissionFactorBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("fallback emission factor batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback emission factor batch"} $code"

    fun projectedLoad(): Double = round((ceiling - endsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackEmissionFactorBatch {
        return FallbackEmissionFactorBatch(
            code = code,
            name = name,
            endsAt = endsAt,
            contactEmail = contactEmail,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback emission factor batch in status draft. */
fun newFallbackEmissionFactorBatch(code: String, name: String): FallbackEmissionFactorBatch {
    return FallbackEmissionFactorBatch(
        code = code,
        name = name,
        endsAt = 0,
        contactEmail = "",
        ceiling = 0.0,
        status = FallbackEmissionFactorBatchStatus.DRAFT,
    )
}
