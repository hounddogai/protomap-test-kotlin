package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor quota is in its life cycle. */
enum class ScheduledEmissionFactorQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled emission factor quota. Its code is unique within a tenant. */
data class ScheduledEmissionFactorQuota(
    var code: String,
    var title: String,
    var checksum: String,
    var discount: Double,
    var attributes: MutableMap<String, String>,
    var budget: Double,
    var status: ScheduledEmissionFactorQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorQuotaStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorQuotaStatus.REQUESTED -> return next == ScheduledEmissionFactorQuotaStatus.GRANTED
            ScheduledEmissionFactorQuotaStatus.GRANTED -> return next == ScheduledEmissionFactorQuotaStatus.EXPIRED
            ScheduledEmissionFactorQuotaStatus.REVOKED -> return next == ScheduledEmissionFactorQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round((discount - budget) * 100) / 100

    fun snapshot(): ScheduledEmissionFactorQuota {
        return ScheduledEmissionFactorQuota(
            code = code,
            title = title,
            checksum = checksum,
            discount = discount,
            attributes = attributes,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled emission factor quota in status requested. */
fun newScheduledEmissionFactorQuota(code: String, title: String): ScheduledEmissionFactorQuota {
    return ScheduledEmissionFactorQuota(
        code = code,
        title = title,
        checksum = "",
        discount = 0.0,
        attributes = mutableMapOf<String, String>(),
        budget = 0.0,
        status = ScheduledEmissionFactorQuotaStatus.REQUESTED,
    )
}
