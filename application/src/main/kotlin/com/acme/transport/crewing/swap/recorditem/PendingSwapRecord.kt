package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending swap record records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a pending swap record is in its life cycle. */
enum class PendingSwapRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending swap record. Its code is unique within a tenant. */
data class PendingSwapRecord(
    var code: String,
    var label: String,
    var odometer: Int,
    var distanceKm: Double,
    var priority: Int,
    var visible: Boolean,
    var status: PendingSwapRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingSwapRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingSwapRecordStatus): Boolean {
        when (status) {
            PendingSwapRecordStatus.NEW -> {
                when (next) {
                    PendingSwapRecordStatus.RUNNING, PendingSwapRecordStatus.FAILED -> return true
                }
                return false
            }
            PendingSwapRecordStatus.QUEUED -> {
                when (next) {
                    PendingSwapRecordStatus.DONE -> return true
                }
                return false
            }
            PendingSwapRecordStatus.RUNNING -> {
                when (next) {
                    PendingSwapRecordStatus.DONE, PendingSwapRecordStatus.FAILED -> return true
                }
                return false
            }
            PendingSwapRecordStatus.DONE -> return next == PendingSwapRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingSwapRecordStatus) {
        if (!canMoveTo(next)) {
            error("pending swap record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round((odometer.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): PendingSwapRecord {
        return PendingSwapRecord(
            code = code,
            label = label,
            odometer = odometer,
            distanceKm = distanceKm,
            priority = priority,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending swap record in status new. */
fun newPendingSwapRecord(code: String, label: String): PendingSwapRecord {
    return PendingSwapRecord(
        code = code,
        label = label,
        odometer = 0,
        distanceKm = 0.0,
        priority = 0,
        visible = false,
        status = PendingSwapRecordStatus.NEW,
    )
}
