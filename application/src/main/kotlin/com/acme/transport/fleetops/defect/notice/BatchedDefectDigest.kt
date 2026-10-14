package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize

/** Where a batched defect digest is in its life cycle. */
enum class BatchedDefectDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched defect digest, with what the planners need to act on it. */
data class BatchedDefectDigest(
    var code: String,
    var label: String,
    var source: String,
    var region: String,
    var checksum: String,
    var quantity: Int,
    var status: BatchedDefectDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDefectDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDefectDigestStatus): Boolean {
        when (status) {
            BatchedDefectDigestStatus.CREATED -> {
                when (next) {
                    BatchedDefectDigestStatus.VALIDATED, BatchedDefectDigestStatus.RETIRED -> return true
                }
                return false
            }
            BatchedDefectDigestStatus.VALIDATED -> return next == BatchedDefectDigestStatus.PUBLISHED || next == BatchedDefectDigestStatus.RETIRED
            BatchedDefectDigestStatus.PUBLISHED -> return next == BatchedDefectDigestStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedDefectDigestStatus) {
        if (!canMoveTo(next)) {
            error("batched defect digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched defect digest"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): BatchedDefectDigest {
        return BatchedDefectDigest(
            code = code,
            label = label,
            source = source,
            region = region,
            checksum = checksum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched defect digest in status created. */
fun newBatchedDefectDigest(code: String, label: String): BatchedDefectDigest {
    return BatchedDefectDigest(
        code = code,
        label = label,
        source = "",
        region = "",
        checksum = "",
        quantity = 0,
        status = BatchedDefectDigestStatus.CREATED,
    )
}
