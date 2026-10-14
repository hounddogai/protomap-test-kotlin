package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration batch records of customs's declaration work, and the rules, stores, and calculations around
// them.

/** Where a declaration batch is in its life cycle. */
enum class DeclarationBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A declaration batch. Its code is unique within a tenant. */
data class DeclarationBatch(
    var code: String,
    var title: String,
    var pallets: Int,
    var stopCount: Int,
    var archived: Boolean,
    var status: DeclarationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationBatchStatus): Boolean {
        when (status) {
            DeclarationBatchStatus.PLANNED -> {
                when (next) {
                    DeclarationBatchStatus.SCHEDULED, DeclarationBatchStatus.COMPLETED -> return true
                }
                return false
            }
            DeclarationBatchStatus.SCHEDULED -> return next == DeclarationBatchStatus.IN_PROGRESS || next == DeclarationBatchStatus.CANCELLED
            DeclarationBatchStatus.IN_PROGRESS -> {
                when (next) {
                    DeclarationBatchStatus.COMPLETED, DeclarationBatchStatus.CANCELLED -> return true
                }
                return false
            }
            DeclarationBatchStatus.COMPLETED -> return next == DeclarationBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DeclarationBatchStatus) {
        if (!canMoveTo(next)) {
            error("declaration batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"declaration batch"} $code"

    fun projectedExposure(): Double = round((pallets.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DeclarationBatch {
        return DeclarationBatch(
            code = code,
            title = title,
            pallets = pallets,
            stopCount = stopCount,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration batch in status planned. */
fun newDeclarationBatch(code: String, title: String): DeclarationBatch {
    return DeclarationBatch(
        code = code,
        title = title,
        pallets = 0,
        stopCount = 0,
        archived = false,
        status = DeclarationBatchStatus.PLANNED,
    )
}
