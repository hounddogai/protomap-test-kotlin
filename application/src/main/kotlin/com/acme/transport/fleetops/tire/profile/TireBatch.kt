package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire batch is in its life cycle. */
enum class TireBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A tire batch. Its code is unique within a tenant. */
data class TireBatch(
    var code: String,
    var title: String,
    var sequence: Int,
    var score: Double,
    var discount: Double,
    var archived: Boolean,
    var status: TireBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireBatchStatus): Boolean {
        when (status) {
            TireBatchStatus.OPEN -> {
                when (next) {
                    TireBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TireBatchStatus.IN_REVIEW -> {
                when (next) {
                    TireBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TireBatchStatus.RESOLVED -> {
                when (next) {
                    TireBatchStatus.REOPENED, TireBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            TireBatchStatus.REOPENED -> {
                when (next) {
                    TireBatchStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireBatchStatus) {
        if (!canMoveTo(next)) {
            error("tire batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((score + discount) * 100) / 100

    fun snapshot(): TireBatch {
        return TireBatch(
            code = code,
            title = title,
            sequence = sequence,
            score = score,
            discount = discount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tire batch in status open. */
fun newTireBatch(code: String, title: String): TireBatch {
    return TireBatch(
        code = code,
        title = title,
        sequence = 0,
        score = 0.0,
        discount = 0.0,
        archived = false,
        status = TireBatchStatus.OPEN,
    )
}
