package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped dwell batch is in its life cycle. */
enum class CappedDwellBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped dwell batch as the dwell team keeps it. */
data class CappedDwellBatch(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var ceiling: Double,
    var reviewer: String,
    var loadKg: Double,
    var batchSize: Int,
    var version: Int,
    var sequence: Int,
    var minimum: Double,
    var quantity: Int,
    var status: CappedDwellBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDwellBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDwellBatchStatus): Boolean {
        when (status) {
            CappedDwellBatchStatus.DRAFT -> {
                when (next) {
                    CappedDwellBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedDwellBatchStatus.PENDING -> {
                when (next) {
                    CappedDwellBatchStatus.ACTIVE, CappedDwellBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedDwellBatchStatus.ACTIVE -> {
                when (next) {
                    CappedDwellBatchStatus.SUSPENDED, CappedDwellBatchStatus.CLOSED -> return true
                }
                return false
            }
            CappedDwellBatchStatus.SUSPENDED -> return next == CappedDwellBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedDwellBatchStatus) {
        if (!canMoveTo(next)) {
            error("capped dwell batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round(loadKg * minimum * 100) / 100

    fun snapshot(): CappedDwellBatch {
        return CappedDwellBatch(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            ceiling = ceiling,
            reviewer = reviewer,
            loadKg = loadKg,
            batchSize = batchSize,
            version = version,
            sequence = sequence,
            minimum = minimum,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped dwell batch in status draft. */
fun newCappedDwellBatch(code: String, name: String): CappedDwellBatch {
    return CappedDwellBatch(
        code = code,
        name = name,
        fuelLiters = 0.0,
        ceiling = 0.0,
        reviewer = "",
        loadKg = 0.0,
        batchSize = 0,
        version = 0,
        sequence = 0,
        minimum = 0.0,
        quantity = 0,
        status = CappedDwellBatchStatus.DRAFT,
    )
}
