package com.acme.transport.terminal.crossdock.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock record records of terminal's cross dock work, and the rules, stores, and calculations around
// them.

/** Where a cross dock record is in its life cycle. */
enum class CrossDockRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a cross dock record and its current status. */
data class CrossDockRecord(
    var code: String,
    var title: String,
    var odometer: Int,
    var score: Double,
    var ceiling: Double,
    var limit: Int,
    var status: CrossDockRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockRecordStatus): Boolean {
        when (status) {
            CrossDockRecordStatus.REQUESTED -> {
                when (next) {
                    CrossDockRecordStatus.REVOKED -> return true
                }
                return false
            }
            CrossDockRecordStatus.GRANTED -> {
                when (next) {
                    CrossDockRecordStatus.EXPIRED -> return true
                }
                return false
            }
            CrossDockRecordStatus.REVOKED -> {
                when (next) {
                    CrossDockRecordStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CrossDockRecordStatus) {
        if (!canMoveTo(next)) {
            error("cross dock record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cross dock record"} $code"

    fun netValue(): Double = round(odometer.toDouble() * ceiling * 100) / 100

    fun snapshot(): CrossDockRecord {
        return CrossDockRecord(
            code = code,
            title = title,
            odometer = odometer,
            score = score,
            ceiling = ceiling,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock record in status requested. */
fun newCrossDockRecord(code: String, title: String): CrossDockRecord {
    return CrossDockRecord(
        code = code,
        title = title,
        odometer = 0,
        score = 0.0,
        ceiling = 0.0,
        limit = 0,
        status = CrossDockRecordStatus.REQUESTED,
    )
}
