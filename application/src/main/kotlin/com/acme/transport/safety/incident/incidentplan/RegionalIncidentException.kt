package com.acme.transport.safety.incident.incidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional incident exception is in its life cycle. */
enum class RegionalIncidentExceptionStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One regional incident exception, with what the planners need to act on it. */
data class RegionalIncidentException(
    var code: String,
    var name: String,
    var revision: Int,
    var trailerId: String,
    var currency: String,
    var loadKg: Double,
    var status: RegionalIncidentExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentExceptionStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentExceptionStatus): Boolean {
        when (status) {
            RegionalIncidentExceptionStatus.REQUESTED -> {
                when (next) {
                    RegionalIncidentExceptionStatus.GRANTED, RegionalIncidentExceptionStatus.REVOKED -> return true
                }
                return false
            }
            RegionalIncidentExceptionStatus.GRANTED -> return next == RegionalIncidentExceptionStatus.REVOKED
            RegionalIncidentExceptionStatus.REVOKED -> {
                when (next) {
                    RegionalIncidentExceptionStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalIncidentExceptionStatus) {
        if (!canMoveTo(next)) {
            error("regional incident exception $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"regional incident exception"} $code"

    fun projectedExposure(): Double = round((revision.toDouble() - loadKg) * 100) / 100

    fun snapshot(): RegionalIncidentException {
        return RegionalIncidentException(
            code = code,
            name = name,
            revision = revision,
            trailerId = trailerId,
            currency = currency,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional incident exception in status requested. */
fun newRegionalIncidentException(code: String, name: String): RegionalIncidentException {
    return RegionalIncidentException(
        code = code,
        name = name,
        revision = 0,
        trailerId = "",
        currency = "",
        loadKg = 0.0,
        status = RegionalIncidentExceptionStatus.REQUESTED,
    )
}
