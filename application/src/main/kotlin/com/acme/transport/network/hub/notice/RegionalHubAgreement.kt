package com.acme.transport.network.hub.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional hub agreement is in its life cycle. */
enum class RegionalHubAgreementStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional hub agreement. Its code is unique within a tenant. */
data class RegionalHubAgreement(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var reviewer: String,
    var etaMinutes: Int,
    var budget: Double,
    var channel: String,
    var currency: String,
    var status: RegionalHubAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalHubAgreementStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalHubAgreementStatus): Boolean {
        when (status) {
            RegionalHubAgreementStatus.REQUESTED -> return next == RegionalHubAgreementStatus.REVOKED || next == RegionalHubAgreementStatus.EXPIRED
            RegionalHubAgreementStatus.GRANTED -> {
                when (next) {
                    RegionalHubAgreementStatus.REVOKED, RegionalHubAgreementStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalHubAgreementStatus.REVOKED -> {
                when (next) {
                    RegionalHubAgreementStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalHubAgreementStatus) {
        if (!canMoveTo(next)) {
            error("regional hub agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional hub agreement"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((budget - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RegionalHubAgreement {
        return RegionalHubAgreement(
            code = code,
            name = name,
            attributes = attributes,
            reviewer = reviewer,
            etaMinutes = etaMinutes,
            budget = budget,
            channel = channel,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional hub agreement in status requested. */
fun newRegionalHubAgreement(code: String, name: String): RegionalHubAgreement {
    return RegionalHubAgreement(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        reviewer = "",
        etaMinutes = 0,
        budget = 0.0,
        channel = "",
        currency = "",
        status = RegionalHubAgreementStatus.REQUESTED,
    )
}
