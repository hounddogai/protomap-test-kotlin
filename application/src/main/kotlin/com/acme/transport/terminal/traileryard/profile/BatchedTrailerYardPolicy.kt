package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched trailer yard policy is in its life cycle. */
enum class BatchedTrailerYardPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched trailer yard policy as the trailer yard team keeps it. */
data class BatchedTrailerYardPolicy(
    var code: String,
    var label: String,
    var ceiling: Double,
    var batchSize: Int,
    var visible: Boolean,
    var description: String,
    var notes: String,
    var endsAt: Int,
    var vehicleId: String,
    var status: BatchedTrailerYardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerYardPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerYardPolicyStatus): Boolean {
        when (status) {
            BatchedTrailerYardPolicyStatus.CREATED -> return next == BatchedTrailerYardPolicyStatus.PUBLISHED || next == BatchedTrailerYardPolicyStatus.RETIRED
            BatchedTrailerYardPolicyStatus.VALIDATED -> return next == BatchedTrailerYardPolicyStatus.PUBLISHED || next == BatchedTrailerYardPolicyStatus.RETIRED
            BatchedTrailerYardPolicyStatus.PUBLISHED -> {
                when (next) {
                    BatchedTrailerYardPolicyStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerYardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer yard policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched trailer yard policy"} $code"

    fun projectedLoad(): Double = round(endsAt.toDouble() * ceiling * 100) / 100

    fun snapshot(): BatchedTrailerYardPolicy {
        return BatchedTrailerYardPolicy(
            code = code,
            label = label,
            ceiling = ceiling,
            batchSize = batchSize,
            visible = visible,
            description = description,
            notes = notes,
            endsAt = endsAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched trailer yard policy in status created. */
fun newBatchedTrailerYardPolicy(code: String, label: String): BatchedTrailerYardPolicy {
    return BatchedTrailerYardPolicy(
        code = code,
        label = label,
        ceiling = 0.0,
        batchSize = 0,
        visible = false,
        description = "",
        notes = "",
        endsAt = 0,
        vehicleId = "",
        status = BatchedTrailerYardPolicyStatus.CREATED,
    )
}
