package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize

/** Where a rest break record is in its life cycle. */
enum class RestBreakRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rest break record as the rest break team keeps it. */
data class RestBreakRecord(
    var code: String,
    var label: String,
    var shiftCode: String,
    var loadKg: Double,
    var source: String,
    var status: RestBreakRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakRecordStatus): Boolean {
        when (status) {
            RestBreakRecordStatus.PROPOSED -> {
                when (next) {
                    RestBreakRecordStatus.REJECTED, RestBreakRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakRecordStatus.APPROVED -> {
                when (next) {
                    RestBreakRecordStatus.REJECTED, RestBreakRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            RestBreakRecordStatus.REJECTED -> return next == RestBreakRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RestBreakRecordStatus) {
        if (!canMoveTo(next)) {
            error("rest break record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rest break record"} $code"

    fun measuredValue(): Double = loadKg

    fun snapshot(): RestBreakRecord {
        return RestBreakRecord(
            code = code,
            label = label,
            shiftCode = shiftCode,
            loadKg = loadKg,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rest break record in status proposed. */
fun newRestBreakRecord(code: String, label: String): RestBreakRecord {
    return RestBreakRecord(
        code = code,
        label = label,
        shiftCode = "",
        loadKg = 0.0,
        source = "",
        status = RestBreakRecordStatus.PROPOSED,
    )
}
