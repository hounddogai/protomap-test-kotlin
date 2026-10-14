package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped tariff batch records of customs's tariff work, and the rules, stores, and calculations around
// them.

/** Where a capped tariff batch is in its life cycle. */
enum class CappedTariffBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped tariff batch, with what the planners need to act on it. */
data class CappedTariffBatch(
    var code: String,
    var title: String,
    var floorValue: Double,
    var createdAt: Int,
    var sequence: Int,
    var budget: Double,
    var driverId: String,
    var notes: String,
    var phone: String,
    var status: CappedTariffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTariffBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTariffBatchStatus): Boolean {
        when (status) {
            CappedTariffBatchStatus.IDLE -> {
                when (next) {
                    CappedTariffBatchStatus.DRAINING -> return true
                }
                return false
            }
            CappedTariffBatchStatus.WARMING -> return next == CappedTariffBatchStatus.READY
            CappedTariffBatchStatus.READY -> {
                when (next) {
                    CappedTariffBatchStatus.DRAINING -> return true
                }
                return false
            }
            CappedTariffBatchStatus.DRAINING -> {
                when (next) {
                    CappedTariffBatchStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTariffBatchStatus) {
        if (!canMoveTo(next)) {
            error("capped tariff batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped tariff batch"} $code"

    fun weightedTotal(): Double = round((createdAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): CappedTariffBatch {
        return CappedTariffBatch(
            code = code,
            title = title,
            floorValue = floorValue,
            createdAt = createdAt,
            sequence = sequence,
            budget = budget,
            driverId = driverId,
            notes = notes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped tariff batch in status idle. */
fun newCappedTariffBatch(code: String, title: String): CappedTariffBatch {
    return CappedTariffBatch(
        code = code,
        title = title,
        floorValue = 0.0,
        createdAt = 0,
        sequence = 0,
        budget = 0.0,
        driverId = "",
        notes = "",
        phone = "",
        status = CappedTariffBatchStatus.IDLE,
    )
}
