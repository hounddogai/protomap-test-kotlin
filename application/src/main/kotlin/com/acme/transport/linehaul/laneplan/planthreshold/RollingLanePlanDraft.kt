package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling lane plan draft is in its life cycle. */
enum class RollingLanePlanDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling lane plan draft as the lane plan team keeps it. */
data class RollingLanePlanDraft(
    var code: String,
    var name: String,
    var batchSize: Int,
    var endsAt: Int,
    var maximum: Double,
    var stopCount: Int,
    var depot: String,
    var dock: String,
    var status: RollingLanePlanDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingLanePlanDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingLanePlanDraftStatus): Boolean {
        when (status) {
            RollingLanePlanDraftStatus.IDLE -> return next == RollingLanePlanDraftStatus.WARMING || next == RollingLanePlanDraftStatus.READY
            RollingLanePlanDraftStatus.WARMING -> {
                when (next) {
                    RollingLanePlanDraftStatus.READY -> return true
                }
                return false
            }
            RollingLanePlanDraftStatus.READY -> return next == RollingLanePlanDraftStatus.DRAINING
            RollingLanePlanDraftStatus.DRAINING -> return next == RollingLanePlanDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingLanePlanDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling lane plan draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((batchSize.toDouble() + maximum) * 100) / 100

    fun snapshot(): RollingLanePlanDraft {
        return RollingLanePlanDraft(
            code = code,
            name = name,
            batchSize = batchSize,
            endsAt = endsAt,
            maximum = maximum,
            stopCount = stopCount,
            depot = depot,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling lane plan draft in status idle. */
fun newRollingLanePlanDraft(code: String, name: String): RollingLanePlanDraft {
    return RollingLanePlanDraft(
        code = code,
        name = name,
        batchSize = 0,
        endsAt = 0,
        maximum = 0.0,
        stopCount = 0,
        depot = "",
        dock = "",
        status = RollingLanePlanDraftStatus.IDLE,
    )
}
