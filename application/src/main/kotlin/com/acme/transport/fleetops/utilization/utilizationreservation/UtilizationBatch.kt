package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization batch is in its life cycle. */
enum class UtilizationBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a utilization batch and its current status. */
data class UtilizationBatch(
    var code: String,
    var title: String,
    var cost: Double,
    var discount: Double,
    var pallets: Int,
    var etaMinutes: Int,
    var amount: Double,
    var weight: Double,
    var status: UtilizationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationBatchStatus): Boolean {
        when (status) {
            UtilizationBatchStatus.NEW -> {
                when (next) {
                    UtilizationBatchStatus.DONE, UtilizationBatchStatus.FAILED -> return true
                }
                return false
            }
            UtilizationBatchStatus.QUEUED -> {
                when (next) {
                    UtilizationBatchStatus.FAILED -> return true
                }
                return false
            }
            UtilizationBatchStatus.RUNNING -> return next == UtilizationBatchStatus.FAILED
            UtilizationBatchStatus.DONE -> {
                when (next) {
                    UtilizationBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationBatchStatus) {
        if (!canMoveTo(next)) {
            error("utilization batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"utilization batch"} $code"

    fun weightedVolume(): Double = round((amount + cost) * 100) / 100

    fun snapshot(): UtilizationBatch {
        return UtilizationBatch(
            code = code,
            title = title,
            cost = cost,
            discount = discount,
            pallets = pallets,
            etaMinutes = etaMinutes,
            amount = amount,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a utilization batch in status new. */
fun newUtilizationBatch(code: String, title: String): UtilizationBatch {
    return UtilizationBatch(
        code = code,
        title = title,
        cost = 0.0,
        discount = 0.0,
        pallets = 0,
        etaMinutes = 0,
        amount = 0.0,
        weight = 0.0,
        status = UtilizationBatchStatus.NEW,
    )
}
