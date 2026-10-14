package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered incident baseline is in its life cycle. */
enum class TieredIncidentBaselineStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered incident baseline, with what the planners need to act on it. */
data class TieredIncidentBaseline(
    var code: String,
    var name: String,
    var active: Boolean,
    var loadKg: Double,
    var etaMinutes: Int,
    var status: TieredIncidentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIncidentBaselineStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIncidentBaselineStatus): Boolean {
        when (status) {
            TieredIncidentBaselineStatus.CREATED -> {
                when (next) {
                    TieredIncidentBaselineStatus.VALIDATED, TieredIncidentBaselineStatus.RETIRED -> return true
                }
                return false
            }
            TieredIncidentBaselineStatus.VALIDATED -> return next == TieredIncidentBaselineStatus.RETIRED
            TieredIncidentBaselineStatus.PUBLISHED -> return next == TieredIncidentBaselineStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredIncidentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("tiered incident baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round(etaMinutes.toDouble() * loadKg * 100) / 100

    fun snapshot(): TieredIncidentBaseline {
        return TieredIncidentBaseline(
            code = code,
            name = name,
            active = active,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered incident baseline in status created. */
fun newTieredIncidentBaseline(code: String, name: String): TieredIncidentBaseline {
    return TieredIncidentBaseline(
        code = code,
        name = name,
        active = false,
        loadKg = 0.0,
        etaMinutes = 0,
        status = TieredIncidentBaselineStatus.CREATED,
    )
}
