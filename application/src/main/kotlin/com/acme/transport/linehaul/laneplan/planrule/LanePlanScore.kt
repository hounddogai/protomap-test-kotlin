package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan score is in its life cycle. */
enum class LanePlanScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lane plan score as the lane plan team keeps it. */
data class LanePlanScore(
    var code: String,
    var name: String,
    var category: String,
    var retries: Int,
    var unit: String,
    var stopCount: Int,
    var carrier: String,
    var timezone: String,
    var status: LanePlanScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanScoreStatus): Boolean {
        when (status) {
            LanePlanScoreStatus.CREATED -> {
                when (next) {
                    LanePlanScoreStatus.VALIDATED, LanePlanScoreStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanScoreStatus.VALIDATED -> {
                when (next) {
                    LanePlanScoreStatus.PUBLISHED, LanePlanScoreStatus.RETIRED -> return true
                }
                return false
            }
            LanePlanScoreStatus.PUBLISHED -> return next == LanePlanScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LanePlanScoreStatus) {
        if (!canMoveTo(next)) {
            error("lane plan score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((retries.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): LanePlanScore {
        return LanePlanScore(
            code = code,
            name = name,
            category = category,
            retries = retries,
            unit = unit,
            stopCount = stopCount,
            carrier = carrier,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lane plan score in status created. */
fun newLanePlanScore(code: String, name: String): LanePlanScore {
    return LanePlanScore(
        code = code,
        name = name,
        category = "",
        retries = 0,
        unit = "",
        stopCount = 0,
        carrier = "",
        timezone = "",
        status = LanePlanScoreStatus.CREATED,
    )
}
