package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a broker quota is in its life cycle. */
enum class BrokerQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A broker quota as the broker team keeps it. */
data class BrokerQuota(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var region: String,
    var sequence: Int,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var status: BrokerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerQuotaStatus): Boolean {
        when (status) {
            BrokerQuotaStatus.PROPOSED -> {
                when (next) {
                    BrokerQuotaStatus.REJECTED, BrokerQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerQuotaStatus.APPROVED -> {
                when (next) {
                    BrokerQuotaStatus.REJECTED, BrokerQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
            BrokerQuotaStatus.REJECTED -> {
                when (next) {
                    BrokerQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("broker quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"broker quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedValue(): Double = round((windowDays.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): BrokerQuota {
        return BrokerQuota(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            region = region,
            sequence = sequence,
            attributes = attributes,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a broker quota in status proposed. */
fun newBrokerQuota(code: String, name: String): BrokerQuota {
    return BrokerQuota(
        code = code,
        name = name,
        etaMinutes = 0,
        windowDays = 0,
        region = "",
        sequence = 0,
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        status = BrokerQuotaStatus.PROPOSED,
    )
}
