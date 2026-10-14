package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered sortation batch is in its life cycle. */
enum class TieredSortationBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered sortation batch. Its code is unique within a tenant. */
data class TieredSortationBatch(
    var code: String,
    var name: String,
    var endsAt: Int,
    var updatedAt: Int,
    var shiftCode: String,
    var loadKg: Double,
    var status: TieredSortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredSortationBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredSortationBatchStatus): Boolean {
        when (status) {
            TieredSortationBatchStatus.REQUESTED -> return next == TieredSortationBatchStatus.GRANTED || next == TieredSortationBatchStatus.REVOKED
            TieredSortationBatchStatus.GRANTED -> return next == TieredSortationBatchStatus.REVOKED || next == TieredSortationBatchStatus.EXPIRED
            TieredSortationBatchStatus.REVOKED -> return next == TieredSortationBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredSortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("tiered sortation batch $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((endsAt.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): TieredSortationBatch {
        return TieredSortationBatch(
            code = code,
            name = name,
            endsAt = endsAt,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered sortation batch in status requested. */
fun newTieredSortationBatch(code: String, name: String): TieredSortationBatch {
    return TieredSortationBatch(
        code = code,
        name = name,
        endsAt = 0,
        updatedAt = 0,
        shiftCode = "",
        loadKg = 0.0,
        status = TieredSortationBatchStatus.REQUESTED,
    )
}
