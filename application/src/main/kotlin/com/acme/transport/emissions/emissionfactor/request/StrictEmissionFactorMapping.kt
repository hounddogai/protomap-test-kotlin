package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict emission factor mapping is in its life cycle. */
enum class StrictEmissionFactorMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict emission factor mapping as the emission factor team keeps it. */
data class StrictEmissionFactorMapping(
    var code: String,
    var title: String,
    var createdAt: Int,
    var startsAt: Int,
    var version: Int,
    var dock: String,
    var shiftCode: String,
    var status: StrictEmissionFactorMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictEmissionFactorMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictEmissionFactorMappingStatus): Boolean {
        when (status) {
            StrictEmissionFactorMappingStatus.IDLE -> return next == StrictEmissionFactorMappingStatus.DRAINING
            StrictEmissionFactorMappingStatus.WARMING -> return next == StrictEmissionFactorMappingStatus.READY
            StrictEmissionFactorMappingStatus.READY -> return next == StrictEmissionFactorMappingStatus.DRAINING
            StrictEmissionFactorMappingStatus.DRAINING -> return next == StrictEmissionFactorMappingStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictEmissionFactorMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict emission factor mapping $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round(startsAt.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): StrictEmissionFactorMapping {
        return StrictEmissionFactorMapping(
            code = code,
            title = title,
            createdAt = createdAt,
            startsAt = startsAt,
            version = version,
            dock = dock,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict emission factor mapping in status idle. */
fun newStrictEmissionFactorMapping(code: String, title: String): StrictEmissionFactorMapping {
    return StrictEmissionFactorMapping(
        code = code,
        title = title,
        createdAt = 0,
        startsAt = 0,
        version = 0,
        dock = "",
        shiftCode = "",
        status = StrictEmissionFactorMappingStatus.IDLE,
    )
}
