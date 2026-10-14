package com.acme.transport.freightpay.ratecard.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card audit records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card audit is in its life cycle. */
enum class RateCardAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a rate card audit and its current status. */
data class RateCardAudit(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var weight: Double,
    var unit: String,
    var vehicleId: String,
    var country: String,
    var status: RateCardAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardAuditStatus): Boolean {
        when (status) {
            RateCardAuditStatus.REQUESTED -> {
                when (next) {
                    RateCardAuditStatus.REVOKED, RateCardAuditStatus.EXPIRED -> return true
                }
                return false
            }
            RateCardAuditStatus.GRANTED -> return next == RateCardAuditStatus.REVOKED || next == RateCardAuditStatus.EXPIRED
            RateCardAuditStatus.REVOKED -> {
                when (next) {
                    RateCardAuditStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardAuditStatus) {
        if (!canMoveTo(next)) {
            error("rate card audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rate card audit"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round((weight + sequence.toDouble()) * 100) / 100

    fun snapshot(): RateCardAudit {
        return RateCardAudit(
            code = code,
            title = title,
            attributes = attributes,
            sequence = sequence,
            weight = weight,
            unit = unit,
            vehicleId = vehicleId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rate card audit in status requested. */
fun newRateCardAudit(code: String, title: String): RateCardAudit {
    return RateCardAudit(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        weight = 0.0,
        unit = "",
        vehicleId = "",
        country = "",
        status = RateCardAuditStatus.REQUESTED,
    )
}
