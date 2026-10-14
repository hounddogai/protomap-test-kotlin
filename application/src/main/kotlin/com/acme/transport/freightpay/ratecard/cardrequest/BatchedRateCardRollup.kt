package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rate card rollup is in its life cycle. */
enum class BatchedRateCardRollupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched rate card rollup. Its code is unique within a tenant. */
data class BatchedRateCardRollup(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var odometer: Int,
    var distanceKm: Double,
    var label: String,
    var attributes: MutableMap<String, String>,
    var status: BatchedRateCardRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRateCardRollupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRateCardRollupStatus): Boolean {
        when (status) {
            BatchedRateCardRollupStatus.PROPOSED -> return next == BatchedRateCardRollupStatus.REJECTED
            BatchedRateCardRollupStatus.APPROVED -> {
                when (next) {
                    BatchedRateCardRollupStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedRateCardRollupStatus.REJECTED -> return next == BatchedRateCardRollupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedRateCardRollupStatus) {
        if (!canMoveTo(next)) {
            error("batched rate card rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched rate card rollup"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((odometer.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): BatchedRateCardRollup {
        return BatchedRateCardRollup(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            odometer = odometer,
            distanceKm = distanceKm,
            label = label,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched rate card rollup in status proposed. */
fun newBatchedRateCardRollup(code: String, name: String): BatchedRateCardRollup {
    return BatchedRateCardRollup(
        code = code,
        name = name,
        etaMinutes = 0,
        odometer = 0,
        distanceKm = 0.0,
        label = "",
        attributes = mutableMapOf<String, String>(),
        status = BatchedRateCardRollupStatus.PROPOSED,
    )
}
