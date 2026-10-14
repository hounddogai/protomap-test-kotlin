package com.acme.transport.network.servicearea.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict service area batch is in its life cycle. */
enum class StrictServiceAreaBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict service area batch and its current status. */
data class StrictServiceAreaBatch(
    var code: String,
    var label: String,
    var version: Int,
    var ratio: Double,
    var threshold: Double,
    var currency: String,
    var status: StrictServiceAreaBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictServiceAreaBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictServiceAreaBatchStatus): Boolean {
        when (status) {
            StrictServiceAreaBatchStatus.PROPOSED -> return next == StrictServiceAreaBatchStatus.APPROVED
            StrictServiceAreaBatchStatus.APPROVED -> {
                when (next) {
                    StrictServiceAreaBatchStatus.REJECTED -> return true
                }
                return false
            }
            StrictServiceAreaBatchStatus.REJECTED -> return next == StrictServiceAreaBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictServiceAreaBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict service area batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round((ratio - version.toDouble()) * 100) / 100

    fun snapshot(): StrictServiceAreaBatch {
        return StrictServiceAreaBatch(
            code = code,
            label = label,
            version = version,
            ratio = ratio,
            threshold = threshold,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict service area batch in status proposed. */
fun newStrictServiceAreaBatch(code: String, label: String): StrictServiceAreaBatch {
    return StrictServiceAreaBatch(
        code = code,
        label = label,
        version = 0,
        ratio = 0.0,
        threshold = 0.0,
        currency = "",
        status = StrictServiceAreaBatchStatus.PROPOSED,
    )
}
