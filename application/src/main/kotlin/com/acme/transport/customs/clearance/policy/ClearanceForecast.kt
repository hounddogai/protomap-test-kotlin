package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance forecast is in its life cycle. */
enum class ClearanceForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A clearance forecast. Its code is unique within a tenant. */
data class ClearanceForecast(
    var code: String,
    var title: String,
    var endsAt: Int,
    var active: Boolean,
    var priority: Int,
    var status: ClearanceForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceForecastStatus): Boolean {
        when (status) {
            ClearanceForecastStatus.NEW -> return next == ClearanceForecastStatus.QUEUED || next == ClearanceForecastStatus.RUNNING
            ClearanceForecastStatus.QUEUED -> {
                when (next) {
                    ClearanceForecastStatus.FAILED -> return true
                }
                return false
            }
            ClearanceForecastStatus.RUNNING -> return next == ClearanceForecastStatus.DONE
            ClearanceForecastStatus.DONE -> {
                when (next) {
                    ClearanceForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceForecastStatus) {
        if (!canMoveTo(next)) {
            error("clearance forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"clearance forecast"} $code"

    fun adjustedValue(): Double = round(endsAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ClearanceForecast {
        return ClearanceForecast(
            code = code,
            title = title,
            endsAt = endsAt,
            active = active,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance forecast in status new. */
fun newClearanceForecast(code: String, title: String): ClearanceForecast {
    return ClearanceForecast(
        code = code,
        title = title,
        endsAt = 0,
        active = false,
        priority = 0,
        status = ClearanceForecastStatus.NEW,
    )
}
