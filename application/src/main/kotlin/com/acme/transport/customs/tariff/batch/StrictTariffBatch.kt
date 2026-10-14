package com.acme.transport.customs.tariff.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict tariff batch records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a strict tariff batch is in its life cycle. */
enum class StrictTariffBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict tariff batch. Its code is unique within a tenant. */
data class StrictTariffBatch(
    var code: String,
    var label: String,
    var segment: String,
    var expiresAt: Int,
    var attempts: Int,
    var ratio: Double,
    var status: StrictTariffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTariffBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTariffBatchStatus): Boolean {
        when (status) {
            StrictTariffBatchStatus.DRAFT -> {
                when (next) {
                    StrictTariffBatchStatus.ACTIVE, StrictTariffBatchStatus.CLOSED -> return true
                }
                return false
            }
            StrictTariffBatchStatus.PENDING -> return next == StrictTariffBatchStatus.CLOSED
            StrictTariffBatchStatus.ACTIVE -> return next == StrictTariffBatchStatus.SUSPENDED || next == StrictTariffBatchStatus.CLOSED
            StrictTariffBatchStatus.SUSPENDED -> {
                when (next) {
                    StrictTariffBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTariffBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict tariff batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict tariff batch"} $code"

    fun adjustedExposure(): Double = round((expiresAt.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): StrictTariffBatch {
        return StrictTariffBatch(
            code = code,
            label = label,
            segment = segment,
            expiresAt = expiresAt,
            attempts = attempts,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict tariff batch in status draft. */
fun newStrictTariffBatch(code: String, label: String): StrictTariffBatch {
    return StrictTariffBatch(
        code = code,
        label = label,
        segment = "",
        expiresAt = 0,
        attempts = 0,
        ratio = 0.0,
        status = StrictTariffBatchStatus.DRAFT,
    )
}
