package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped incident baseline records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a capped incident baseline is in its life cycle. */
enum class CappedIncidentBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped incident baseline. Its code is unique within a tenant. */
data class CappedIncidentBaseline(
    var code: String,
    var label: String,
    var batchSize: Int,
    var etaMinutes: Int,
    var timezone: String,
    var country: String,
    var visible: Boolean,
    var active: Boolean,
    var floorValue: Double,
    var minimum: Double,
    var loadKg: Double,
    var status: CappedIncidentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentBaselineStatus): Boolean {
        when (status) {
            CappedIncidentBaselineStatus.REQUESTED -> {
                when (next) {
                    CappedIncidentBaselineStatus.GRANTED -> return true
                }
                return false
            }
            CappedIncidentBaselineStatus.GRANTED -> {
                when (next) {
                    CappedIncidentBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            CappedIncidentBaselineStatus.REVOKED -> return next == CappedIncidentBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedIncidentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped incident baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped incident baseline"} $code"

    fun effectiveTotal(): Double = round((floorValue - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CappedIncidentBaseline {
        return CappedIncidentBaseline(
            code = code,
            label = label,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            timezone = timezone,
            country = country,
            visible = visible,
            active = active,
            floorValue = floorValue,
            minimum = minimum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped incident baseline in status requested. */
fun newCappedIncidentBaseline(code: String, label: String): CappedIncidentBaseline {
    return CappedIncidentBaseline(
        code = code,
        label = label,
        batchSize = 0,
        etaMinutes = 0,
        timezone = "",
        country = "",
        visible = false,
        active = false,
        floorValue = 0.0,
        minimum = 0.0,
        loadKg = 0.0,
        status = CappedIncidentBaselineStatus.REQUESTED,
    )
}
