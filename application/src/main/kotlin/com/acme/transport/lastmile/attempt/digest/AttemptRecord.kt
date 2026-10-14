package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

/** Where a attempt record is in its life cycle. */
enum class AttemptRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A attempt record. Its code is unique within a tenant. */
data class AttemptRecord(
    var code: String,
    var name: String,
    var endsAt: Int,
    var timezone: String,
    var source: String,
    var lane: String,
    var trailerId: String,
    var status: AttemptRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptRecordStatus): Boolean {
        when (status) {
            AttemptRecordStatus.IDLE -> return next == AttemptRecordStatus.WARMING || next == AttemptRecordStatus.DRAINING
            AttemptRecordStatus.WARMING -> return next == AttemptRecordStatus.READY || next == AttemptRecordStatus.DRAINING
            AttemptRecordStatus.READY -> {
                when (next) {
                    AttemptRecordStatus.DRAINING, AttemptRecordStatus.STOPPED -> return true
                }
                return false
            }
            AttemptRecordStatus.DRAINING -> {
                when (next) {
                    AttemptRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptRecordStatus) {
        if (!canMoveTo(next)) {
            error("attempt record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AttemptRecord {
        return AttemptRecord(
            code = code,
            name = name,
            endsAt = endsAt,
            timezone = timezone,
            source = source,
            lane = lane,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a attempt record in status idle. */
fun newAttemptRecord(code: String, name: String): AttemptRecord {
    return AttemptRecord(
        code = code,
        name = name,
        endsAt = 0,
        timezone = "",
        source = "",
        lane = "",
        trailerId = "",
        status = AttemptRecordStatus.IDLE,
    )
}
