package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff batch is in its life cycle. */
enum class TariffBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tariff batch and its current status. */
data class TariffBatch(
    var code: String,
    var title: String,
    var timezone: String,
    var priority: Int,
    var trailerId: String,
    var attempts: Int,
    var summary: String,
    var quantity: Int,
    var batchSize: Int,
    var carrier: String,
    var driverId: String,
    var status: TariffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffBatchStatus): Boolean {
        when (status) {
            TariffBatchStatus.NEW -> {
                when (next) {
                    TariffBatchStatus.QUEUED -> return true
                }
                return false
            }
            TariffBatchStatus.QUEUED -> return next == TariffBatchStatus.RUNNING || next == TariffBatchStatus.DONE
            TariffBatchStatus.RUNNING -> return next == TariffBatchStatus.DONE || next == TariffBatchStatus.FAILED
            TariffBatchStatus.DONE -> return next == TariffBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffBatchStatus) {
        if (!canMoveTo(next)) {
            error("tariff batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveVolume(): Double = round((batchSize.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): TariffBatch {
        return TariffBatch(
            code = code,
            title = title,
            timezone = timezone,
            priority = priority,
            trailerId = trailerId,
            attempts = attempts,
            summary = summary,
            quantity = quantity,
            batchSize = batchSize,
            carrier = carrier,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff batch in status new. */
fun newTariffBatch(code: String, title: String): TariffBatch {
    return TariffBatch(
        code = code,
        title = title,
        timezone = "",
        priority = 0,
        trailerId = "",
        attempts = 0,
        summary = "",
        quantity = 0,
        batchSize = 0,
        carrier = "",
        driverId = "",
        status = TariffBatchStatus.NEW,
    )
}
