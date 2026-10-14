package com.acme.transport.crewing.roster.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster record is in its life cycle. */
enum class RosterRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One roster record, with what the planners need to act on it. */
data class RosterRecord(
    var code: String,
    var title: String,
    var score: Double,
    var phone: String,
    var createdAt: Int,
    var status: RosterRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterRecordStatus): Boolean {
        when (status) {
            RosterRecordStatus.IDLE -> return next == RosterRecordStatus.READY
            RosterRecordStatus.WARMING -> {
                when (next) {
                    RosterRecordStatus.DRAINING, RosterRecordStatus.STOPPED -> return true
                }
                return false
            }
            RosterRecordStatus.READY -> {
                when (next) {
                    RosterRecordStatus.DRAINING -> return true
                }
                return false
            }
            RosterRecordStatus.DRAINING -> return next == RosterRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RosterRecordStatus) {
        if (!canMoveTo(next)) {
            error("roster record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster record"} $code"

    fun netTotal(): Double = round((createdAt.toDouble() - score) * 100) / 100

    fun snapshot(): RosterRecord {
        return RosterRecord(
            code = code,
            title = title,
            score = score,
            phone = phone,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a roster record in status idle. */
fun newRosterRecord(code: String, title: String): RosterRecord {
    return RosterRecord(
        code = code,
        title = title,
        score = 0.0,
        phone = "",
        createdAt = 0,
        status = RosterRecordStatus.IDLE,
    )
}
