package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender batch records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender batch is in its life cycle. */
enum class TenderBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tender batch and its current status. */
data class TenderBatch(
    var code: String,
    var name: String,
    var floorValue: Double,
    var pallets: Int,
    var revision: Int,
    var loadKg: Double,
    var durationMinutes: Int,
    var title: String,
    var status: TenderBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderBatchStatus): Boolean {
        when (status) {
            TenderBatchStatus.REQUESTED -> {
                when (next) {
                    TenderBatchStatus.GRANTED -> return true
                }
                return false
            }
            TenderBatchStatus.GRANTED -> return next == TenderBatchStatus.REVOKED
            TenderBatchStatus.REVOKED -> return next == TenderBatchStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TenderBatchStatus) {
        if (!canMoveTo(next)) {
            error("tender batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"tender batch"} $code"

    fun netValue(): Double = round((revision.toDouble() - loadKg) * 100) / 100

    fun snapshot(): TenderBatch {
        return TenderBatch(
            code = code,
            name = name,
            floorValue = floorValue,
            pallets = pallets,
            revision = revision,
            loadKg = loadKg,
            durationMinutes = durationMinutes,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tender batch in status requested. */
fun newTenderBatch(code: String, name: String): TenderBatch {
    return TenderBatch(
        code = code,
        name = name,
        floorValue = 0.0,
        pallets = 0,
        revision = 0,
        loadKg = 0.0,
        durationMinutes = 0,
        title = "",
        status = TenderBatchStatus.REQUESTED,
    )
}
