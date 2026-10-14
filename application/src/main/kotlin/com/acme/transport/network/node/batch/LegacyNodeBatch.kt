package com.acme.transport.network.node.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy node batch records of network's node work, and the rules, stores, and calculations around them.

/** Where a legacy node batch is in its life cycle. */
enum class LegacyNodeBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy node batch. Its code is unique within a tenant. */
data class LegacyNodeBatch(
    var code: String,
    var title: String,
    var depot: String,
    var phone: String,
    var description: String,
    var distanceKm: Double,
    var weight: Double,
    var batchSize: Int,
    var archived: Boolean,
    var trailerId: String,
    var status: LegacyNodeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNodeBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNodeBatchStatus): Boolean {
        when (status) {
            LegacyNodeBatchStatus.PLANNED -> return next == LegacyNodeBatchStatus.COMPLETED
            LegacyNodeBatchStatus.SCHEDULED -> {
                when (next) {
                    LegacyNodeBatchStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyNodeBatchStatus.IN_PROGRESS -> return next == LegacyNodeBatchStatus.COMPLETED || next == LegacyNodeBatchStatus.CANCELLED
            LegacyNodeBatchStatus.COMPLETED -> {
                when (next) {
                    LegacyNodeBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyNodeBatchStatus) {
        if (!canMoveTo(next)) {
            error("legacy node batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((distanceKm + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LegacyNodeBatch {
        return LegacyNodeBatch(
            code = code,
            title = title,
            depot = depot,
            phone = phone,
            description = description,
            distanceKm = distanceKm,
            weight = weight,
            batchSize = batchSize,
            archived = archived,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy node batch in status planned. */
fun newLegacyNodeBatch(code: String, title: String): LegacyNodeBatch {
    return LegacyNodeBatch(
        code = code,
        title = title,
        depot = "",
        phone = "",
        description = "",
        distanceKm = 0.0,
        weight = 0.0,
        batchSize = 0,
        archived = false,
        trailerId = "",
        status = LegacyNodeBatchStatus.PLANNED,
    )
}
