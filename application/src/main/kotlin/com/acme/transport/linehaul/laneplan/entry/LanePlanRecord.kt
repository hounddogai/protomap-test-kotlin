package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan record is in its life cycle. */
enum class LanePlanRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lane plan record as the lane plan team keeps it. */
data class LanePlanRecord(
    var code: String,
    var title: String,
    var depot: String,
    var tags: MutableList<String>,
    var startsAt: Int,
    var score: Double,
    var floorValue: Double,
    var status: LanePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanRecordStatus): Boolean {
        when (status) {
            LanePlanRecordStatus.PROPOSED -> return next == LanePlanRecordStatus.REJECTED || next == LanePlanRecordStatus.WITHDRAWN
            LanePlanRecordStatus.APPROVED -> return next == LanePlanRecordStatus.WITHDRAWN
            LanePlanRecordStatus.REJECTED -> {
                when (next) {
                    LanePlanRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("lane plan record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lane plan record"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((score + floorValue) * 100) / 100

    fun snapshot(): LanePlanRecord {
        return LanePlanRecord(
            code = code,
            title = title,
            depot = depot,
            tags = tags,
            startsAt = startsAt,
            score = score,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lane plan record in status proposed. */
fun newLanePlanRecord(code: String, title: String): LanePlanRecord {
    return LanePlanRecord(
        code = code,
        title = title,
        depot = "",
        tags = mutableListOf<String>(),
        startsAt = 0,
        score = 0.0,
        floorValue = 0.0,
        status = LanePlanRecordStatus.PROPOSED,
    )
}
