package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional incident agreement is in its life cycle. */
enum class RegionalIncidentAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional incident agreement. Its code is unique within a tenant. */
data class RegionalIncidentAgreement(
    var code: String,
    var title: String,
    var attempts: Int,
    var margin: Double,
    var durationMinutes: Int,
    var expiresAt: Int,
    var channel: String,
    var score: Double,
    var trailerId: String,
    var graceDays: Int,
    var version: Int,
    var status: RegionalIncidentAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentAgreementStatus): Boolean {
        when (status) {
            RegionalIncidentAgreementStatus.OPEN -> {
                when (next) {
                    RegionalIncidentAgreementStatus.REOPENED -> return true
                }
                return false
            }
            RegionalIncidentAgreementStatus.IN_REVIEW -> return next == RegionalIncidentAgreementStatus.RESOLVED || next == RegionalIncidentAgreementStatus.ARCHIVED
            RegionalIncidentAgreementStatus.RESOLVED -> {
                when (next) {
                    RegionalIncidentAgreementStatus.REOPENED, RegionalIncidentAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalIncidentAgreementStatus.REOPENED -> return next == RegionalIncidentAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalIncidentAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional incident agreement $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((attempts.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): RegionalIncidentAgreement {
        return RegionalIncidentAgreement(
            code = code,
            title = title,
            attempts = attempts,
            margin = margin,
            durationMinutes = durationMinutes,
            expiresAt = expiresAt,
            channel = channel,
            score = score,
            trailerId = trailerId,
            graceDays = graceDays,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional incident agreement in status open. */
fun newRegionalIncidentAgreement(code: String, title: String): RegionalIncidentAgreement {
    return RegionalIncidentAgreement(
        code = code,
        title = title,
        attempts = 0,
        margin = 0.0,
        durationMinutes = 0,
        expiresAt = 0,
        channel = "",
        score = 0.0,
        trailerId = "",
        graceDays = 0,
        version = 0,
        status = RegionalIncidentAgreementStatus.OPEN,
    )
}
