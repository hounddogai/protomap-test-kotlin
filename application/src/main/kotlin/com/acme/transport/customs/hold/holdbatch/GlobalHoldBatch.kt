package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global hold batch records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a global hold batch is in its life cycle. */
enum class GlobalHoldBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global hold batch and its current status. */
data class GlobalHoldBatch(
    var code: String,
    var label: String,
    var floorValue: Double,
    var unit: String,
    var segment: String,
    var retries: Int,
    var status: GlobalHoldBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalHoldBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalHoldBatchStatus): Boolean {
        when (status) {
            GlobalHoldBatchStatus.NEW -> {
                when (next) {
                    GlobalHoldBatchStatus.QUEUED, GlobalHoldBatchStatus.DONE -> return true
                }
                return false
            }
            GlobalHoldBatchStatus.QUEUED -> return next == GlobalHoldBatchStatus.FAILED
            GlobalHoldBatchStatus.RUNNING -> return next == GlobalHoldBatchStatus.DONE || next == GlobalHoldBatchStatus.FAILED
            GlobalHoldBatchStatus.DONE -> return next == GlobalHoldBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalHoldBatchStatus) {
        if (!canMoveTo(next)) {
            error("global hold batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round((floorValue + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalHoldBatch {
        return GlobalHoldBatch(
            code = code,
            label = label,
            floorValue = floorValue,
            unit = unit,
            segment = segment,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global hold batch in status new. */
fun newGlobalHoldBatch(code: String, label: String): GlobalHoldBatch {
    return GlobalHoldBatch(
        code = code,
        label = label,
        floorValue = 0.0,
        unit = "",
        segment = "",
        retries = 0,
        status = GlobalHoldBatchStatus.NEW,
    )
}
