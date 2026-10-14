package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.text.normalize

/** Where a batched hub tier is in its life cycle. */
enum class BatchedHubTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched hub tier, with what the planners need to act on it. */
data class BatchedHubTier(
    var code: String,
    var title: String,
    var driverId: String,
    var sequence: Int,
    var phone: String,
    var status: BatchedHubTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHubTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHubTierStatus): Boolean {
        when (status) {
            BatchedHubTierStatus.PLANNED -> {
                when (next) {
                    BatchedHubTierStatus.SCHEDULED -> return true
                }
                return false
            }
            BatchedHubTierStatus.SCHEDULED -> return next == BatchedHubTierStatus.COMPLETED
            BatchedHubTierStatus.IN_PROGRESS -> return next == BatchedHubTierStatus.CANCELLED
            BatchedHubTierStatus.COMPLETED -> {
                when (next) {
                    BatchedHubTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedHubTierStatus) {
        if (!canMoveTo(next)) {
            error("batched hub tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): BatchedHubTier {
        return BatchedHubTier(
            code = code,
            title = title,
            driverId = driverId,
            sequence = sequence,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched hub tier in status planned. */
fun newBatchedHubTier(code: String, title: String): BatchedHubTier {
    return BatchedHubTier(
        code = code,
        title = title,
        driverId = "",
        sequence = 0,
        phone = "",
        status = BatchedHubTierStatus.PLANNED,
    )
}
