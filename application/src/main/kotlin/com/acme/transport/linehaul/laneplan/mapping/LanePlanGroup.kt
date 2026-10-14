package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan group is in its life cycle. */
enum class LanePlanGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One lane plan group, with what the planners need to act on it. */
data class LanePlanGroup(
    var code: String,
    var title: String,
    var margin: Double,
    var tags: MutableList<String>,
    var sequence: Int,
    var checksum: String,
    var attempts: Int,
    var status: LanePlanGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanGroupStatus): Boolean {
        when (status) {
            LanePlanGroupStatus.IDLE -> {
                when (next) {
                    LanePlanGroupStatus.WARMING, LanePlanGroupStatus.READY -> return true
                }
                return false
            }
            LanePlanGroupStatus.WARMING -> return next == LanePlanGroupStatus.DRAINING
            LanePlanGroupStatus.READY -> return next == LanePlanGroupStatus.DRAINING || next == LanePlanGroupStatus.STOPPED
            LanePlanGroupStatus.DRAINING -> {
                when (next) {
                    LanePlanGroupStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanGroupStatus) {
        if (!canMoveTo(next)) {
            error("lane plan group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(margin * sequence.toDouble() * 100) / 100

    fun snapshot(): LanePlanGroup {
        return LanePlanGroup(
            code = code,
            title = title,
            margin = margin,
            tags = tags,
            sequence = sequence,
            checksum = checksum,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lane plan group in status idle. */
fun newLanePlanGroup(code: String, title: String): LanePlanGroup {
    return LanePlanGroup(
        code = code,
        title = title,
        margin = 0.0,
        tags = mutableListOf<String>(),
        sequence = 0,
        checksum = "",
        attempts = 0,
        status = LanePlanGroupStatus.IDLE,
    )
}
