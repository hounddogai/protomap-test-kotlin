package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.kit.text.normalize

/** Where a emission factor score is in its life cycle. */
enum class EmissionFactorScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a emission factor score and its current status. */
data class EmissionFactorScore(
    var code: String,
    var title: String,
    var rate: Double,
    var contactEmail: String,
    var channel: String,
    var locale: String,
    var status: EmissionFactorScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorScoreStatus): Boolean {
        when (status) {
            EmissionFactorScoreStatus.REQUESTED -> return next == EmissionFactorScoreStatus.GRANTED
            EmissionFactorScoreStatus.GRANTED -> {
                when (next) {
                    EmissionFactorScoreStatus.REVOKED -> return true
                }
                return false
            }
            EmissionFactorScoreStatus.REVOKED -> return next == EmissionFactorScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: EmissionFactorScoreStatus) {
        if (!canMoveTo(next)) {
            error("emission factor score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"emission factor score"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): EmissionFactorScore {
        return EmissionFactorScore(
            code = code,
            title = title,
            rate = rate,
            contactEmail = contactEmail,
            channel = channel,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a emission factor score in status requested. */
fun newEmissionFactorScore(code: String, title: String): EmissionFactorScore {
    return EmissionFactorScore(
        code = code,
        title = title,
        rate = 0.0,
        contactEmail = "",
        channel = "",
        locale = "",
        status = EmissionFactorScoreStatus.REQUESTED,
    )
}
