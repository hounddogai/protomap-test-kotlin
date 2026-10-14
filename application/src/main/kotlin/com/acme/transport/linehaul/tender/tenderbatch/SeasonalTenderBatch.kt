package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal tender batch records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a seasonal tender batch is in its life cycle. */
enum class SeasonalTenderBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a seasonal tender batch and its current status. */
data class SeasonalTenderBatch(
    var code: String,
    var title: String,
    var stopCount: Int,
    var shiftCode: String,
    var phone: String,
    var amount: Double,
    var discount: Double,
    var pallets: Int,
    var status: SeasonalTenderBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTenderBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTenderBatchStatus): Boolean {
        when (status) {
            SeasonalTenderBatchStatus.PLANNED -> return next == SeasonalTenderBatchStatus.IN_PROGRESS || next == SeasonalTenderBatchStatus.CANCELLED
            SeasonalTenderBatchStatus.SCHEDULED -> return next == SeasonalTenderBatchStatus.CANCELLED
            SeasonalTenderBatchStatus.IN_PROGRESS -> return next == SeasonalTenderBatchStatus.COMPLETED || next == SeasonalTenderBatchStatus.CANCELLED
            SeasonalTenderBatchStatus.COMPLETED -> {
                when (next) {
                    SeasonalTenderBatchStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTenderBatchStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tender batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal tender batch"} $code"

    fun adjustedVolume(): Double = round((pallets.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalTenderBatch {
        return SeasonalTenderBatch(
            code = code,
            title = title,
            stopCount = stopCount,
            shiftCode = shiftCode,
            phone = phone,
            amount = amount,
            discount = discount,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal tender batch in status planned. */
fun newSeasonalTenderBatch(code: String, title: String): SeasonalTenderBatch {
    return SeasonalTenderBatch(
        code = code,
        title = title,
        stopCount = 0,
        shiftCode = "",
        phone = "",
        amount = 0.0,
        discount = 0.0,
        pallets = 0,
        status = SeasonalTenderBatchStatus.PLANNED,
    )
}
