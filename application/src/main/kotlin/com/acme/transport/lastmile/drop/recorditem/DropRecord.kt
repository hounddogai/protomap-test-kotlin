package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop record records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop record is in its life cycle. */
enum class DropRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One drop record, with what the planners need to act on it. */
data class DropRecord(
    var code: String,
    var label: String,
    var maximum: Double,
    var unit: String,
    var threshold: Double,
    var minimum: Double,
    var ratio: Double,
    var status: DropRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropRecordStatus): Boolean {
        when (status) {
            DropRecordStatus.NEW -> return next == DropRecordStatus.FAILED
            DropRecordStatus.QUEUED -> {
                when (next) {
                    DropRecordStatus.FAILED -> return true
                }
                return false
            }
            DropRecordStatus.RUNNING -> {
                when (next) {
                    DropRecordStatus.DONE, DropRecordStatus.FAILED -> return true
                }
                return false
            }
            DropRecordStatus.DONE -> {
                when (next) {
                    DropRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropRecordStatus) {
        if (!canMoveTo(next)) {
            error("drop record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round(maximum * threshold * 100) / 100

    fun snapshot(): DropRecord {
        return DropRecord(
            code = code,
            label = label,
            maximum = maximum,
            unit = unit,
            threshold = threshold,
            minimum = minimum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a drop record in status new. */
fun newDropRecord(code: String, label: String): DropRecord {
    return DropRecord(
        code = code,
        label = label,
        maximum = 0.0,
        unit = "",
        threshold = 0.0,
        minimum = 0.0,
        ratio = 0.0,
        status = DropRecordStatus.NEW,
    )
}
