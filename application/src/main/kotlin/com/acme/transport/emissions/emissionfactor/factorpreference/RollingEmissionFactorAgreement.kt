package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling emission factor agreement is in its life cycle. */
enum class RollingEmissionFactorAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling emission factor agreement. Its code is unique within a tenant. */
data class RollingEmissionFactorAgreement(
    var code: String,
    var label: String,
    var channel: String,
    var createdAt: Int,
    var lane: String,
    var retries: Int,
    var source: String,
    var minimum: Double,
    var status: RollingEmissionFactorAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingEmissionFactorAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingEmissionFactorAgreementStatus): Boolean {
        when (status) {
            RollingEmissionFactorAgreementStatus.CREATED -> {
                when (next) {
                    RollingEmissionFactorAgreementStatus.PUBLISHED, RollingEmissionFactorAgreementStatus.RETIRED -> return true
                }
                return false
            }
            RollingEmissionFactorAgreementStatus.VALIDATED -> {
                when (next) {
                    RollingEmissionFactorAgreementStatus.RETIRED -> return true
                }
                return false
            }
            RollingEmissionFactorAgreementStatus.PUBLISHED -> return next == RollingEmissionFactorAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingEmissionFactorAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rolling emission factor agreement $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling emission factor agreement"} $code"

    fun adjustedTotal(): Double = round((minimum + retries.toDouble()) * 100) / 100

    fun snapshot(): RollingEmissionFactorAgreement {
        return RollingEmissionFactorAgreement(
            code = code,
            label = label,
            channel = channel,
            createdAt = createdAt,
            lane = lane,
            retries = retries,
            source = source,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling emission factor agreement in status created. */
fun newRollingEmissionFactorAgreement(code: String, label: String): RollingEmissionFactorAgreement {
    return RollingEmissionFactorAgreement(
        code = code,
        label = label,
        channel = "",
        createdAt = 0,
        lane = "",
        retries = 0,
        source = "",
        minimum = 0.0,
        status = RollingEmissionFactorAgreementStatus.CREATED,
    )
}
