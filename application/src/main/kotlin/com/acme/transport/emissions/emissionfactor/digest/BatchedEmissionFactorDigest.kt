package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched emission factor digest records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a batched emission factor digest is in its life cycle. */
enum class BatchedEmissionFactorDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched emission factor digest. Its code is unique within a tenant. */
data class BatchedEmissionFactorDigest(
    var code: String,
    var title: String,
    var threshold: Double,
    var windowDays: Int,
    var odometer: Int,
    var status: BatchedEmissionFactorDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEmissionFactorDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEmissionFactorDigestStatus): Boolean {
        when (status) {
            BatchedEmissionFactorDigestStatus.PROPOSED -> return next == BatchedEmissionFactorDigestStatus.APPROVED
            BatchedEmissionFactorDigestStatus.APPROVED -> return next == BatchedEmissionFactorDigestStatus.REJECTED || next == BatchedEmissionFactorDigestStatus.WITHDRAWN
            BatchedEmissionFactorDigestStatus.REJECTED -> return next == BatchedEmissionFactorDigestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedEmissionFactorDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched emission factor digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round((threshold - odometer.toDouble()) * 100) / 100

    fun snapshot(): BatchedEmissionFactorDigest {
        return BatchedEmissionFactorDigest(
            code = code,
            title = title,
            threshold = threshold,
            windowDays = windowDays,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched emission factor digest in status proposed. */
fun newBatchedEmissionFactorDigest(code: String, title: String): BatchedEmissionFactorDigest {
    return BatchedEmissionFactorDigest(
        code = code,
        title = title,
        threshold = 0.0,
        windowDays = 0,
        odometer = 0,
        status = BatchedEmissionFactorDigestStatus.PROPOSED,
    )
}
