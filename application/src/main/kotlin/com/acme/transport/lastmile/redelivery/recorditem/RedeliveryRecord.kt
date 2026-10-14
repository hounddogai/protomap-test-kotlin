package com.acme.transport.lastmile.redelivery.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery record records of lastmile's redelivery work, and the rules, stores, and calculations around
// them.

/** Where a redelivery record is in its life cycle. */
enum class RedeliveryRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One redelivery record, with what the planners need to act on it. */
data class RedeliveryRecord(
    var code: String,
    var name: String,
    var ceiling: Double,
    var graceDays: Int,
    var title: String,
    var status: RedeliveryRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryRecordStatus): Boolean {
        when (status) {
            RedeliveryRecordStatus.PLANNED -> {
                when (next) {
                    RedeliveryRecordStatus.SCHEDULED, RedeliveryRecordStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RedeliveryRecordStatus.SCHEDULED -> {
                when (next) {
                    RedeliveryRecordStatus.IN_PROGRESS, RedeliveryRecordStatus.COMPLETED -> return true
                }
                return false
            }
            RedeliveryRecordStatus.IN_PROGRESS -> {
                when (next) {
                    RedeliveryRecordStatus.CANCELLED -> return true
                }
                return false
            }
            RedeliveryRecordStatus.COMPLETED -> return next == RedeliveryRecordStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RedeliveryRecordStatus) {
        if (!canMoveTo(next)) {
            error("redelivery record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((graceDays.toDouble() - ceiling) * 100) / 100

    fun snapshot(): RedeliveryRecord {
        return RedeliveryRecord(
            code = code,
            name = name,
            ceiling = ceiling,
            graceDays = graceDays,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery record in status planned. */
fun newRedeliveryRecord(code: String, name: String): RedeliveryRecord {
    return RedeliveryRecord(
        code = code,
        name = name,
        ceiling = 0.0,
        graceDays = 0,
        title = "",
        status = RedeliveryRecordStatus.PLANNED,
    )
}
