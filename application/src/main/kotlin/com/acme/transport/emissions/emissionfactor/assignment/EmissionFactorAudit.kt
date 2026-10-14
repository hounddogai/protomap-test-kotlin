package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor audit is in its life cycle. */
enum class EmissionFactorAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A emission factor audit as the emission factor team keeps it. */
data class EmissionFactorAudit(
    var code: String,
    var name: String,
    var locale: String,
    var startsAt: Int,
    var contactEmail: String,
    var discount: Double,
    var expiresAt: Int,
    var status: EmissionFactorAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorAuditStatus): Boolean {
        when (status) {
            EmissionFactorAuditStatus.CREATED -> {
                when (next) {
                    EmissionFactorAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            EmissionFactorAuditStatus.VALIDATED -> {
                when (next) {
                    EmissionFactorAuditStatus.PUBLISHED -> return true
                }
                return false
            }
            EmissionFactorAuditStatus.PUBLISHED -> {
                when (next) {
                    EmissionFactorAuditStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorAuditStatus) {
        if (!canMoveTo(next)) {
            error("emission factor audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"emission factor audit"} $code"

    fun effectiveValue(): Double = round((discount - startsAt.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorAudit {
        return EmissionFactorAudit(
            code = code,
            name = name,
            locale = locale,
            startsAt = startsAt,
            contactEmail = contactEmail,
            discount = discount,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a emission factor audit in status created. */
fun newEmissionFactorAudit(code: String, name: String): EmissionFactorAudit {
    return EmissionFactorAudit(
        code = code,
        name = name,
        locale = "",
        startsAt = 0,
        contactEmail = "",
        discount = 0.0,
        expiresAt = 0,
        status = EmissionFactorAuditStatus.CREATED,
    )
}
