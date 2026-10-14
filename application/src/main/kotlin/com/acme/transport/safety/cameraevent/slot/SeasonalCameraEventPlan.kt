package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal camera event plan is in its life cycle. */
enum class SeasonalCameraEventPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal camera event plan as the camera event team keeps it. */
data class SeasonalCameraEventPlan(
    var code: String,
    var title: String,
    var timezone: String,
    var reviewer: String,
    var pinned: Boolean,
    var checksum: String,
    var limit: Int,
    var depot: String,
    var score: Double,
    var retries: Int,
    var status: SeasonalCameraEventPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCameraEventPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCameraEventPlanStatus): Boolean {
        when (status) {
            SeasonalCameraEventPlanStatus.NEW -> return next == SeasonalCameraEventPlanStatus.DONE || next == SeasonalCameraEventPlanStatus.FAILED
            SeasonalCameraEventPlanStatus.QUEUED -> return next == SeasonalCameraEventPlanStatus.DONE
            SeasonalCameraEventPlanStatus.RUNNING -> {
                when (next) {
                    SeasonalCameraEventPlanStatus.DONE, SeasonalCameraEventPlanStatus.FAILED -> return true
                }
                return false
            }
            SeasonalCameraEventPlanStatus.DONE -> {
                when (next) {
                    SeasonalCameraEventPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCameraEventPlanStatus) {
        if (!canMoveTo(next)) {
            error("seasonal camera event plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal camera event plan"} $code"

    fun effectiveExposure(): Double = round(score * limit.toDouble() * 100) / 100

    fun snapshot(): SeasonalCameraEventPlan {
        return SeasonalCameraEventPlan(
            code = code,
            title = title,
            timezone = timezone,
            reviewer = reviewer,
            pinned = pinned,
            checksum = checksum,
            limit = limit,
            depot = depot,
            score = score,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal camera event plan in status new. */
fun newSeasonalCameraEventPlan(code: String, title: String): SeasonalCameraEventPlan {
    return SeasonalCameraEventPlan(
        code = code,
        title = title,
        timezone = "",
        reviewer = "",
        pinned = false,
        checksum = "",
        limit = 0,
        depot = "",
        score = 0.0,
        retries = 0,
        status = SeasonalCameraEventPlanStatus.NEW,
    )
}
