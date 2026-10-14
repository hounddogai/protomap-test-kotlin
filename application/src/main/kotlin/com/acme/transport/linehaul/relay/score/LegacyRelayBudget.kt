package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy relay budget is in its life cycle. */
enum class LegacyRelayBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a legacy relay budget and its current status. */
data class LegacyRelayBudget(
    var code: String,
    var name: String,
    var minimum: Double,
    var distanceKm: Double,
    var updatedAt: Int,
    var threshold: Double,
    var status: LegacyRelayBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyRelayBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyRelayBudgetStatus): Boolean {
        when (status) {
            LegacyRelayBudgetStatus.NEW -> {
                when (next) {
                    LegacyRelayBudgetStatus.FAILED -> return true
                }
                return false
            }
            LegacyRelayBudgetStatus.QUEUED -> {
                when (next) {
                    LegacyRelayBudgetStatus.RUNNING, LegacyRelayBudgetStatus.DONE -> return true
                }
                return false
            }
            LegacyRelayBudgetStatus.RUNNING -> return next == LegacyRelayBudgetStatus.DONE || next == LegacyRelayBudgetStatus.FAILED
            LegacyRelayBudgetStatus.DONE -> {
                when (next) {
                    LegacyRelayBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyRelayBudgetStatus) {
        if (!canMoveTo(next)) {
            error("legacy relay budget $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy relay budget"} $code"

    fun netTotal(): Double = round((minimum + distanceKm) * 100) / 100

    fun snapshot(): LegacyRelayBudget {
        return LegacyRelayBudget(
            code = code,
            name = name,
            minimum = minimum,
            distanceKm = distanceKm,
            updatedAt = updatedAt,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy relay budget in status new. */
fun newLegacyRelayBudget(code: String, name: String): LegacyRelayBudget {
    return LegacyRelayBudget(
        code = code,
        name = name,
        minimum = 0.0,
        distanceKm = 0.0,
        updatedAt = 0,
        threshold = 0.0,
        status = LegacyRelayBudgetStatus.NEW,
    )
}
