package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell exception is in its life cycle. */
enum class DwellExceptionStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dwell exception, with what the planners need to act on it. */
data class DwellException(
    var code: String,
    var name: String,
    var externalRef: String,
    var startsAt: Int,
    var limit: Int,
    var ceiling: Double,
    var capacity: Int,
    var status: DwellExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellExceptionStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellExceptionStatus): Boolean {
        when (status) {
            DwellExceptionStatus.PROPOSED -> return next == DwellExceptionStatus.APPROVED
            DwellExceptionStatus.APPROVED -> {
                when (next) {
                    DwellExceptionStatus.REJECTED, DwellExceptionStatus.WITHDRAWN -> return true
                }
                return false
            }
            DwellExceptionStatus.REJECTED -> return next == DwellExceptionStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DwellExceptionStatus) {
        if (!canMoveTo(next)) {
            error("dwell exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dwell exception"} $code"

    fun weightedLoad(): Double = round((startsAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): DwellException {
        return DwellException(
            code = code,
            name = name,
            externalRef = externalRef,
            startsAt = startsAt,
            limit = limit,
            ceiling = ceiling,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dwell exception in status proposed. */
fun newDwellException(code: String, name: String): DwellException {
    return DwellException(
        code = code,
        name = name,
        externalRef = "",
        startsAt = 0,
        limit = 0,
        ceiling = 0.0,
        capacity = 0,
        status = DwellExceptionStatus.PROPOSED,
    )
}
