package com.acme.transport.terminal.crossdock.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary cross dock record is in its life cycle. */
enum class PrimaryCrossDockRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary cross dock record, with what the planners need to act on it. */
data class PrimaryCrossDockRecord(
    var code: String,
    var label: String,
    var driverId: String,
    var reviewer: String,
    var ceiling: Double,
    var minimum: Double,
    var carrier: String,
    var revision: Int,
    var status: PrimaryCrossDockRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCrossDockRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCrossDockRecordStatus): Boolean {
        when (status) {
            PrimaryCrossDockRecordStatus.NEW -> return next == PrimaryCrossDockRecordStatus.DONE || next == PrimaryCrossDockRecordStatus.FAILED
            PrimaryCrossDockRecordStatus.QUEUED -> {
                when (next) {
                    PrimaryCrossDockRecordStatus.RUNNING, PrimaryCrossDockRecordStatus.DONE -> return true
                }
                return false
            }
            PrimaryCrossDockRecordStatus.RUNNING -> {
                when (next) {
                    PrimaryCrossDockRecordStatus.DONE, PrimaryCrossDockRecordStatus.FAILED -> return true
                }
                return false
            }
            PrimaryCrossDockRecordStatus.DONE -> return next == PrimaryCrossDockRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryCrossDockRecordStatus) {
        if (!canMoveTo(next)) {
            error("primary cross dock record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round((minimum - revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCrossDockRecord {
        return PrimaryCrossDockRecord(
            code = code,
            label = label,
            driverId = driverId,
            reviewer = reviewer,
            ceiling = ceiling,
            minimum = minimum,
            carrier = carrier,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary cross dock record in status new. */
fun newPrimaryCrossDockRecord(code: String, label: String): PrimaryCrossDockRecord {
    return PrimaryCrossDockRecord(
        code = code,
        label = label,
        driverId = "",
        reviewer = "",
        ceiling = 0.0,
        minimum = 0.0,
        carrier = "",
        revision = 0,
        status = PrimaryCrossDockRecordStatus.NEW,
    )
}
