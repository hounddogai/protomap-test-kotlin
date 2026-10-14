package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tire tier is in its life cycle. */
enum class TieredTireTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a tiered tire tier and its current status. */
data class TieredTireTier(
    var code: String,
    var label: String,
    var segment: String,
    var version: Int,
    var revision: Int,
    var etaMinutes: Int,
    var minimum: Double,
    var priority: Int,
    var notes: String,
    var fuelLiters: Double,
    var threshold: Double,
    var status: TieredTireTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireTierStatus): Boolean {
        when (status) {
            TieredTireTierStatus.OPEN -> return next == TieredTireTierStatus.REOPENED || next == TieredTireTierStatus.ARCHIVED
            TieredTireTierStatus.IN_REVIEW -> {
                when (next) {
                    TieredTireTierStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredTireTierStatus.RESOLVED -> {
                when (next) {
                    TieredTireTierStatus.REOPENED, TieredTireTierStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredTireTierStatus.REOPENED -> {
                when (next) {
                    TieredTireTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTireTierStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((etaMinutes.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): TieredTireTier {
        return TieredTireTier(
            code = code,
            label = label,
            segment = segment,
            version = version,
            revision = revision,
            etaMinutes = etaMinutes,
            minimum = minimum,
            priority = priority,
            notes = notes,
            fuelLiters = fuelLiters,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tiered tire tier in status open. */
fun newTieredTireTier(code: String, label: String): TieredTireTier {
    return TieredTireTier(
        code = code,
        label = label,
        segment = "",
        version = 0,
        revision = 0,
        etaMinutes = 0,
        minimum = 0.0,
        priority = 0,
        notes = "",
        fuelLiters = 0.0,
        threshold = 0.0,
        status = TieredTireTierStatus.OPEN,
    )
}
