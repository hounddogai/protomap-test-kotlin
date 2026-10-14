package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.normalize

/** Where a scorecard record is in its life cycle. */
enum class ScorecardRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scorecard record, with what the planners need to act on it. */
data class ScorecardRecord(
    var code: String,
    var name: String,
    var routeCode: String,
    var createdAt: Int,
    var tags: MutableList<String>,
    var status: ScorecardRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardRecordStatus): Boolean {
        when (status) {
            ScorecardRecordStatus.NEW -> {
                when (next) {
                    ScorecardRecordStatus.RUNNING -> return true
                }
                return false
            }
            ScorecardRecordStatus.QUEUED -> return next == ScorecardRecordStatus.RUNNING || next == ScorecardRecordStatus.DONE
            ScorecardRecordStatus.RUNNING -> return next == ScorecardRecordStatus.DONE
            ScorecardRecordStatus.DONE -> return next == ScorecardRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScorecardRecordStatus) {
        if (!canMoveTo(next)) {
            error("scorecard record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scorecard record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): ScorecardRecord {
        return ScorecardRecord(
            code = code,
            name = name,
            routeCode = routeCode,
            createdAt = createdAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scorecard record in status new. */
fun newScorecardRecord(code: String, name: String): ScorecardRecord {
    return ScorecardRecord(
        code = code,
        name = name,
        routeCode = "",
        createdAt = 0,
        tags = mutableListOf<String>(),
        status = ScorecardRecordStatus.NEW,
    )
}
