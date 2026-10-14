package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.text.normalize

// The fallback scorecard batch records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a fallback scorecard batch is in its life cycle. */
enum class FallbackScorecardBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback scorecard batch and its current status. */
data class FallbackScorecardBatch(
    var code: String,
    var label: String,
    var pallets: Int,
    var driverId: String,
    var attributes: MutableMap<String, String>,
    var shiftCode: String,
    var status: FallbackScorecardBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackScorecardBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackScorecardBatchStatus): Boolean {
        when (status) {
            FallbackScorecardBatchStatus.CREATED -> {
                when (next) {
                    FallbackScorecardBatchStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackScorecardBatchStatus.VALIDATED -> return next == FallbackScorecardBatchStatus.PUBLISHED || next == FallbackScorecardBatchStatus.RETIRED
            FallbackScorecardBatchStatus.PUBLISHED -> return next == FallbackScorecardBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackScorecardBatchStatus) {
        if (!canMoveTo(next)) {
            error("fallback scorecard batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): FallbackScorecardBatch {
        return FallbackScorecardBatch(
            code = code,
            label = label,
            pallets = pallets,
            driverId = driverId,
            attributes = attributes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback scorecard batch in status created. */
fun newFallbackScorecardBatch(code: String, label: String): FallbackScorecardBatch {
    return FallbackScorecardBatch(
        code = code,
        label = label,
        pallets = 0,
        driverId = "",
        attributes = mutableMapOf<String, String>(),
        shiftCode = "",
        status = FallbackScorecardBatchStatus.CREATED,
    )
}
