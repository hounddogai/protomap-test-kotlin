package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient tender checkpoint is in its life cycle. */
enum class LenientTenderCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lenient tender checkpoint, with what the planners need to act on it. */
data class LenientTenderCheckpoint(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var minimum: Double,
    var amount: Double,
    var retries: Int,
    var archived: Boolean,
    var carrier: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var rate: Double,
    var status: LenientTenderCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTenderCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTenderCheckpointStatus): Boolean {
        when (status) {
            LenientTenderCheckpointStatus.IDLE -> return next == LenientTenderCheckpointStatus.READY || next == LenientTenderCheckpointStatus.STOPPED
            LenientTenderCheckpointStatus.WARMING -> {
                when (next) {
                    LenientTenderCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            LenientTenderCheckpointStatus.READY -> return next == LenientTenderCheckpointStatus.DRAINING || next == LenientTenderCheckpointStatus.STOPPED
            LenientTenderCheckpointStatus.DRAINING -> return next == LenientTenderCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientTenderCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient tender checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient tender checkpoint"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round(retries.toDouble() * minimum * 100) / 100

    fun snapshot(): LenientTenderCheckpoint {
        return LenientTenderCheckpoint(
            code = code,
            label = label,
            updatedAt = updatedAt,
            minimum = minimum,
            amount = amount,
            retries = retries,
            archived = archived,
            carrier = carrier,
            tags = tags,
            quantity = quantity,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient tender checkpoint in status idle. */
fun newLenientTenderCheckpoint(code: String, label: String): LenientTenderCheckpoint {
    return LenientTenderCheckpoint(
        code = code,
        label = label,
        updatedAt = 0,
        minimum = 0.0,
        amount = 0.0,
        retries = 0,
        archived = false,
        carrier = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        rate = 0.0,
        status = LenientTenderCheckpointStatus.IDLE,
    )
}
