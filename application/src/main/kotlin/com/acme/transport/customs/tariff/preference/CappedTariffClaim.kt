package com.acme.transport.customs.tariff.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tariff claim is in its life cycle. */
enum class CappedTariffClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped tariff claim as the tariff team keeps it. */
data class CappedTariffClaim(
    var code: String,
    var name: String,
    var summary: String,
    var weight: Double,
    var country: String,
    var cost: Double,
    var attributes: MutableMap<String, String>,
    var createdAt: Int,
    var etaMinutes: Int,
    var lane: String,
    var ownerId: String,
    var status: CappedTariffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTariffClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTariffClaimStatus): Boolean {
        when (status) {
            CappedTariffClaimStatus.REQUESTED -> return next == CappedTariffClaimStatus.GRANTED || next == CappedTariffClaimStatus.EXPIRED
            CappedTariffClaimStatus.GRANTED -> {
                when (next) {
                    CappedTariffClaimStatus.REVOKED, CappedTariffClaimStatus.EXPIRED -> return true
                }
                return false
            }
            CappedTariffClaimStatus.REVOKED -> {
                when (next) {
                    CappedTariffClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTariffClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped tariff claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((cost - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CappedTariffClaim {
        return CappedTariffClaim(
            code = code,
            name = name,
            summary = summary,
            weight = weight,
            country = country,
            cost = cost,
            attributes = attributes,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            lane = lane,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped tariff claim in status requested. */
fun newCappedTariffClaim(code: String, name: String): CappedTariffClaim {
    return CappedTariffClaim(
        code = code,
        name = name,
        summary = "",
        weight = 0.0,
        country = "",
        cost = 0.0,
        attributes = mutableMapOf<String, String>(),
        createdAt = 0,
        etaMinutes = 0,
        lane = "",
        ownerId = "",
        status = CappedTariffClaimStatus.REQUESTED,
    )
}
