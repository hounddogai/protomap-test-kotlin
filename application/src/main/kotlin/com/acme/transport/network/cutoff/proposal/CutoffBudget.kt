package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff budget is in its life cycle. */
enum class CutoffBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One cutoff budget, with what the planners need to act on it. */
data class CutoffBudget(
    var code: String,
    var name: String,
    var source: String,
    var distanceKm: Double,
    var retries: Int,
    var stopCount: Int,
    var priority: Int,
    var sequence: Int,
    var startsAt: Int,
    var weight: Double,
    var visible: Boolean,
    var status: CutoffBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffBudgetStatus): Boolean {
        when (status) {
            CutoffBudgetStatus.REQUESTED -> {
                when (next) {
                    CutoffBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            CutoffBudgetStatus.GRANTED -> {
                when (next) {
                    CutoffBudgetStatus.EXPIRED -> return true
                }
                return false
            }
            CutoffBudgetStatus.REVOKED -> return next == CutoffBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CutoffBudgetStatus) {
        if (!canMoveTo(next)) {
            error("cutoff budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((weight - distanceKm) * 100) / 100

    fun snapshot(): CutoffBudget {
        return CutoffBudget(
            code = code,
            name = name,
            source = source,
            distanceKm = distanceKm,
            retries = retries,
            stopCount = stopCount,
            priority = priority,
            sequence = sequence,
            startsAt = startsAt,
            weight = weight,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a cutoff budget in status requested. */
fun newCutoffBudget(code: String, name: String): CutoffBudget {
    return CutoffBudget(
        code = code,
        name = name,
        source = "",
        distanceKm = 0.0,
        retries = 0,
        stopCount = 0,
        priority = 0,
        sequence = 0,
        startsAt = 0,
        weight = 0.0,
        visible = false,
        status = CutoffBudgetStatus.REQUESTED,
    )
}
