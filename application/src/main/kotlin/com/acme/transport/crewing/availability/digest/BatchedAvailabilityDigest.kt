package com.acme.transport.crewing.availability.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched availability digest records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a batched availability digest is in its life cycle. */
enum class BatchedAvailabilityDigestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched availability digest as the availability team keeps it. */
data class BatchedAvailabilityDigest(
    var code: String,
    var label: String,
    var retries: Int,
    var odometer: Int,
    var contactEmail: String,
    var status: BatchedAvailabilityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilityDigestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilityDigestStatus): Boolean {
        when (status) {
            BatchedAvailabilityDigestStatus.NEW -> return next == BatchedAvailabilityDigestStatus.QUEUED || next == BatchedAvailabilityDigestStatus.FAILED
            BatchedAvailabilityDigestStatus.QUEUED -> return next == BatchedAvailabilityDigestStatus.RUNNING
            BatchedAvailabilityDigestStatus.RUNNING -> {
                when (next) {
                    BatchedAvailabilityDigestStatus.FAILED -> return true
                }
                return false
            }
            BatchedAvailabilityDigestStatus.DONE -> return next == BatchedAvailabilityDigestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilityDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched availability digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round(retries.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): BatchedAvailabilityDigest {
        return BatchedAvailabilityDigest(
            code = code,
            label = label,
            retries = retries,
            odometer = odometer,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched availability digest in status new. */
fun newBatchedAvailabilityDigest(code: String, label: String): BatchedAvailabilityDigest {
    return BatchedAvailabilityDigest(
        code = code,
        label = label,
        retries = 0,
        odometer = 0,
        contactEmail = "",
        status = BatchedAvailabilityDigestStatus.NEW,
    )
}
