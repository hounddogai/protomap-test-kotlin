package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.text.normalize

/** Where a legacy incident request is in its life cycle. */
enum class LegacyIncidentRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy incident request and its current status. */
data class LegacyIncidentRequest(
    var code: String,
    var title: String,
    var currency: String,
    var channel: String,
    var description: String,
    var minimum: Double,
    var status: LegacyIncidentRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentRequestStatus): Boolean {
        when (status) {
            LegacyIncidentRequestStatus.REQUESTED -> {
                when (next) {
                    LegacyIncidentRequestStatus.GRANTED -> return true
                }
                return false
            }
            LegacyIncidentRequestStatus.GRANTED -> return next == LegacyIncidentRequestStatus.REVOKED || next == LegacyIncidentRequestStatus.EXPIRED
            LegacyIncidentRequestStatus.REVOKED -> {
                when (next) {
                    LegacyIncidentRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyIncidentRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy incident request"} $code"

    fun measuredValue(): Double = minimum

    fun snapshot(): LegacyIncidentRequest {
        return LegacyIncidentRequest(
            code = code,
            title = title,
            currency = currency,
            channel = channel,
            description = description,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy incident request in status requested. */
fun newLegacyIncidentRequest(code: String, title: String): LegacyIncidentRequest {
    return LegacyIncidentRequest(
        code = code,
        title = title,
        currency = "",
        channel = "",
        description = "",
        minimum = 0.0,
        status = LegacyIncidentRequestStatus.REQUESTED,
    )
}
