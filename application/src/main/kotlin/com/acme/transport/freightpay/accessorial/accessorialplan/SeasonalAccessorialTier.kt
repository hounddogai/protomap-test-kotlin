package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal accessorial tier is in its life cycle. */
enum class SeasonalAccessorialTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal accessorial tier as the accessorial team keeps it. */
data class SeasonalAccessorialTier(
    var code: String,
    var label: String,
    var priority: Int,
    var routeCode: String,
    var loadKg: Double,
    var amount: Double,
    var attempts: Int,
    var archived: Boolean,
    var reviewer: String,
    var status: SeasonalAccessorialTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAccessorialTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAccessorialTierStatus): Boolean {
        when (status) {
            SeasonalAccessorialTierStatus.NEW -> return next == SeasonalAccessorialTierStatus.RUNNING || next == SeasonalAccessorialTierStatus.DONE
            SeasonalAccessorialTierStatus.QUEUED -> return next == SeasonalAccessorialTierStatus.RUNNING
            SeasonalAccessorialTierStatus.RUNNING -> return next == SeasonalAccessorialTierStatus.DONE || next == SeasonalAccessorialTierStatus.FAILED
            SeasonalAccessorialTierStatus.DONE -> {
                when (next) {
                    SeasonalAccessorialTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAccessorialTierStatus) {
        if (!canMoveTo(next)) {
            error("seasonal accessorial tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round((priority.toDouble() + loadKg) * 100) / 100

    fun snapshot(): SeasonalAccessorialTier {
        return SeasonalAccessorialTier(
            code = code,
            label = label,
            priority = priority,
            routeCode = routeCode,
            loadKg = loadKg,
            amount = amount,
            attempts = attempts,
            archived = archived,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal accessorial tier in status new. */
fun newSeasonalAccessorialTier(code: String, label: String): SeasonalAccessorialTier {
    return SeasonalAccessorialTier(
        code = code,
        label = label,
        priority = 0,
        routeCode = "",
        loadKg = 0.0,
        amount = 0.0,
        attempts = 0,
        archived = false,
        reviewer = "",
        status = SeasonalAccessorialTierStatus.NEW,
    )
}
