package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched drop digest is in its life cycle. */
enum class BatchedDropDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a batched drop digest and its current status. */
data class BatchedDropDigest(
    var code: String,
    var title: String,
    var ownerId: String,
    var sequence: Int,
    var externalRef: String,
    var country: String,
    var margin: Double,
    var checksum: String,
    var dock: String,
    var pallets: Int,
    var status: BatchedDropDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDropDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDropDigestStatus): Boolean {
        when (status) {
            BatchedDropDigestStatus.IDLE -> {
                when (next) {
                    BatchedDropDigestStatus.READY, BatchedDropDigestStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDropDigestStatus.WARMING -> {
                when (next) {
                    BatchedDropDigestStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDropDigestStatus.READY -> return next == BatchedDropDigestStatus.DRAINING || next == BatchedDropDigestStatus.STOPPED
            BatchedDropDigestStatus.DRAINING -> {
                when (next) {
                    BatchedDropDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDropDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched drop digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round(sequence.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): BatchedDropDigest {
        return BatchedDropDigest(
            code = code,
            title = title,
            ownerId = ownerId,
            sequence = sequence,
            externalRef = externalRef,
            country = country,
            margin = margin,
            checksum = checksum,
            dock = dock,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched drop digest in status idle. */
fun newBatchedDropDigest(code: String, title: String): BatchedDropDigest {
    return BatchedDropDigest(
        code = code,
        title = title,
        ownerId = "",
        sequence = 0,
        externalRef = "",
        country = "",
        margin = 0.0,
        checksum = "",
        dock = "",
        pallets = 0,
        status = BatchedDropDigestStatus.IDLE,
    )
}
