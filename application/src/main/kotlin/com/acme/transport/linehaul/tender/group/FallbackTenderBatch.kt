package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tender batch is in its life cycle. */
enum class FallbackTenderBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A fallback tender batch. Its code is unique within a tenant. */
data class FallbackTenderBatch(
    var code: String,
    var name: String,
    var batchSize: Int,
    var phone: String,
    var quantity: Int,
    var checksum: String,
    var notes: String,
    var loadKg: Double,
    var status: FallbackTenderBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTenderBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTenderBatchStatus): Boolean {
        when (status) {
            FallbackTenderBatchStatus.REQUESTED -> {
                when (next) {
                    FallbackTenderBatchStatus.REVOKED, FallbackTenderBatchStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackTenderBatchStatus.GRANTED -> {
                when (next) {
                    FallbackTenderBatchStatus.REVOKED, FallbackTenderBatchStatus.EXPIRED -> return true
                }
                return false
            }
            FallbackTenderBatchStatus.REVOKED -> return next == FallbackTenderBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FallbackTenderBatchStatus) {
        if (!canMoveTo(next)) {
            error("fallback tender batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((batchSize.toDouble() + loadKg) * 100) / 100

    fun snapshot(): FallbackTenderBatch {
        return FallbackTenderBatch(
            code = code,
            name = name,
            batchSize = batchSize,
            phone = phone,
            quantity = quantity,
            checksum = checksum,
            notes = notes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback tender batch in status requested. */
fun newFallbackTenderBatch(code: String, name: String): FallbackTenderBatch {
    return FallbackTenderBatch(
        code = code,
        name = name,
        batchSize = 0,
        phone = "",
        quantity = 0,
        checksum = "",
        notes = "",
        loadKg = 0.0,
        status = FallbackTenderBatchStatus.REQUESTED,
    )
}
