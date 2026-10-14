package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive duty period checkpoint is in its life cycle. */
enum class AdaptiveDutyPeriodCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive duty period checkpoint, with what the planners need to act on it. */
data class AdaptiveDutyPeriodCheckpoint(
    var code: String,
    var title: String,
    var region: String,
    var dock: String,
    var graceDays: Int,
    var retries: Int,
    var status: AdaptiveDutyPeriodCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDutyPeriodCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDutyPeriodCheckpointStatus): Boolean {
        when (status) {
            AdaptiveDutyPeriodCheckpointStatus.DRAFT -> return next == AdaptiveDutyPeriodCheckpointStatus.SUSPENDED || next == AdaptiveDutyPeriodCheckpointStatus.CLOSED
            AdaptiveDutyPeriodCheckpointStatus.PENDING -> return next == AdaptiveDutyPeriodCheckpointStatus.ACTIVE || next == AdaptiveDutyPeriodCheckpointStatus.CLOSED
            AdaptiveDutyPeriodCheckpointStatus.ACTIVE -> {
                when (next) {
                    AdaptiveDutyPeriodCheckpointStatus.SUSPENDED, AdaptiveDutyPeriodCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveDutyPeriodCheckpointStatus.SUSPENDED -> return next == AdaptiveDutyPeriodCheckpointStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveDutyPeriodCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("adaptive duty period checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive duty period checkpoint"} $code"

    fun effectiveLoad(): Double = round(retries.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): AdaptiveDutyPeriodCheckpoint {
        return AdaptiveDutyPeriodCheckpoint(
            code = code,
            title = title,
            region = region,
            dock = dock,
            graceDays = graceDays,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive duty period checkpoint in status draft. */
fun newAdaptiveDutyPeriodCheckpoint(code: String, title: String): AdaptiveDutyPeriodCheckpoint {
    return AdaptiveDutyPeriodCheckpoint(
        code = code,
        title = title,
        region = "",
        dock = "",
        graceDays = 0,
        retries = 0,
        status = AdaptiveDutyPeriodCheckpointStatus.DRAFT,
    )
}
