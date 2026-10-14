package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional emission factor target records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a regional emission factor target is in its life cycle. */
enum class RegionalEmissionFactorTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional emission factor target as the emission factor team keeps it. */
data class RegionalEmissionFactorTarget(
    var code: String,
    var name: String,
    var threshold: Double,
    var active: Boolean,
    var distanceKm: Double,
    var summary: String,
    var graceDays: Int,
    var label: String,
    var title: String,
    var startsAt: Int,
    var status: RegionalEmissionFactorTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEmissionFactorTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEmissionFactorTargetStatus): Boolean {
        when (status) {
            RegionalEmissionFactorTargetStatus.IDLE -> return next == RegionalEmissionFactorTargetStatus.WARMING || next == RegionalEmissionFactorTargetStatus.DRAINING
            RegionalEmissionFactorTargetStatus.WARMING -> return next == RegionalEmissionFactorTargetStatus.STOPPED
            RegionalEmissionFactorTargetStatus.READY -> {
                when (next) {
                    RegionalEmissionFactorTargetStatus.DRAINING, RegionalEmissionFactorTargetStatus.STOPPED -> return true
                }
                return false
            }
            RegionalEmissionFactorTargetStatus.DRAINING -> {
                when (next) {
                    RegionalEmissionFactorTargetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalEmissionFactorTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional emission factor target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveExposure(): Double = round((threshold + graceDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalEmissionFactorTarget {
        return RegionalEmissionFactorTarget(
            code = code,
            name = name,
            threshold = threshold,
            active = active,
            distanceKm = distanceKm,
            summary = summary,
            graceDays = graceDays,
            label = label,
            title = title,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional emission factor target in status idle. */
fun newRegionalEmissionFactorTarget(code: String, name: String): RegionalEmissionFactorTarget {
    return RegionalEmissionFactorTarget(
        code = code,
        name = name,
        threshold = 0.0,
        active = false,
        distanceKm = 0.0,
        summary = "",
        graceDays = 0,
        label = "",
        title = "",
        startsAt = 0,
        status = RegionalEmissionFactorTargetStatus.IDLE,
    )
}
