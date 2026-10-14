package com.acme.transport.network.servicearea.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled service area score is in its life cycle. */
enum class ScheduledServiceAreaScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled service area score as the service area team keeps it. */
data class ScheduledServiceAreaScore(
    var code: String,
    var name: String,
    var label: String,
    var graceDays: Int,
    var updatedAt: Int,
    var description: String,
    var pinned: Boolean,
    var status: ScheduledServiceAreaScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServiceAreaScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServiceAreaScoreStatus): Boolean {
        when (status) {
            ScheduledServiceAreaScoreStatus.NEW -> return next == ScheduledServiceAreaScoreStatus.RUNNING || next == ScheduledServiceAreaScoreStatus.FAILED
            ScheduledServiceAreaScoreStatus.QUEUED -> {
                when (next) {
                    ScheduledServiceAreaScoreStatus.DONE -> return true
                }
                return false
            }
            ScheduledServiceAreaScoreStatus.RUNNING -> return next == ScheduledServiceAreaScoreStatus.FAILED
            ScheduledServiceAreaScoreStatus.DONE -> return next == ScheduledServiceAreaScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledServiceAreaScoreStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service area score $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun netExposure(): Double = round((updatedAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): ScheduledServiceAreaScore {
        return ScheduledServiceAreaScore(
            code = code,
            name = name,
            label = label,
            graceDays = graceDays,
            updatedAt = updatedAt,
            description = description,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled service area score in status new. */
fun newScheduledServiceAreaScore(code: String, name: String): ScheduledServiceAreaScore {
    return ScheduledServiceAreaScore(
        code = code,
        name = name,
        label = "",
        graceDays = 0,
        updatedAt = 0,
        description = "",
        pinned = false,
        status = ScheduledServiceAreaScoreStatus.NEW,
    )
}
