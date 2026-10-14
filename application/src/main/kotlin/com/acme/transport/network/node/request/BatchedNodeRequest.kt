package com.acme.transport.network.node.request

import com.acme.transport.kit.text.normalize

// The batched node request records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a batched node request is in its life cycle. */
enum class BatchedNodeRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched node request. Its code is unique within a tenant. */
data class BatchedNodeRequest(
    var code: String,
    var name: String,
    var channel: String,
    var category: String,
    var startsAt: Int,
    var description: String,
    var summary: String,
    var contactEmail: String,
    var timezone: String,
    var checksum: String,
    var status: BatchedNodeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeRequestStatus): Boolean {
        when (status) {
            BatchedNodeRequestStatus.PROPOSED -> {
                when (next) {
                    BatchedNodeRequestStatus.APPROVED -> return true
                }
                return false
            }
            BatchedNodeRequestStatus.APPROVED -> return next == BatchedNodeRequestStatus.REJECTED || next == BatchedNodeRequestStatus.WITHDRAWN
            BatchedNodeRequestStatus.REJECTED -> {
                when (next) {
                    BatchedNodeRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNodeRequestStatus) {
        if (!canMoveTo(next)) {
            error("batched node request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched node request"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): BatchedNodeRequest {
        return BatchedNodeRequest(
            code = code,
            name = name,
            channel = channel,
            category = category,
            startsAt = startsAt,
            description = description,
            summary = summary,
            contactEmail = contactEmail,
            timezone = timezone,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched node request in status proposed. */
fun newBatchedNodeRequest(code: String, name: String): BatchedNodeRequest {
    return BatchedNodeRequest(
        code = code,
        name = name,
        channel = "",
        category = "",
        startsAt = 0,
        description = "",
        summary = "",
        contactEmail = "",
        timezone = "",
        checksum = "",
        status = BatchedNodeRequestStatus.PROPOSED,
    )
}
