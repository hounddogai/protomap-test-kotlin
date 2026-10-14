package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell request is in its life cycle. */
enum class DwellRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A dwell request as the dwell team keeps it. */
data class DwellRequest(
    var code: String,
    var title: String,
    var maximum: Double,
    var sequence: Int,
    var capacity: Int,
    var unit: String,
    var score: Double,
    var durationMinutes: Int,
    var status: DwellRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellRequestStatus): Boolean {
        when (status) {
            DwellRequestStatus.NEW -> {
                when (next) {
                    DwellRequestStatus.QUEUED, DwellRequestStatus.FAILED -> return true
                }
                return false
            }
            DwellRequestStatus.QUEUED -> {
                when (next) {
                    DwellRequestStatus.RUNNING -> return true
                }
                return false
            }
            DwellRequestStatus.RUNNING -> return next == DwellRequestStatus.DONE || next == DwellRequestStatus.FAILED
            DwellRequestStatus.DONE -> {
                when (next) {
                    DwellRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellRequestStatus) {
        if (!canMoveTo(next)) {
            error("dwell request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((maximum - capacity.toDouble()) * 100) / 100

    fun snapshot(): DwellRequest {
        return DwellRequest(
            code = code,
            title = title,
            maximum = maximum,
            sequence = sequence,
            capacity = capacity,
            unit = unit,
            score = score,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dwell request in status new. */
fun newDwellRequest(code: String, title: String): DwellRequest {
    return DwellRequest(
        code = code,
        title = title,
        maximum = 0.0,
        sequence = 0,
        capacity = 0,
        unit = "",
        score = 0.0,
        durationMinutes = 0,
        status = DwellRequestStatus.NEW,
    )
}
