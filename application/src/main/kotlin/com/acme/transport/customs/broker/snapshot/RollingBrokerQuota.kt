package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling broker quota is in its life cycle. */
enum class RollingBrokerQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling broker quota as the broker team keeps it. */
data class RollingBrokerQuota(
    var code: String,
    var label: String,
    var title: String,
    var priority: Int,
    var startsAt: Int,
    var status: RollingBrokerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBrokerQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBrokerQuotaStatus): Boolean {
        when (status) {
            RollingBrokerQuotaStatus.REQUESTED -> {
                when (next) {
                    RollingBrokerQuotaStatus.GRANTED, RollingBrokerQuotaStatus.REVOKED -> return true
                }
                return false
            }
            RollingBrokerQuotaStatus.GRANTED -> {
                when (next) {
                    RollingBrokerQuotaStatus.REVOKED, RollingBrokerQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            RollingBrokerQuotaStatus.REVOKED -> return next == RollingBrokerQuotaStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingBrokerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling broker quota $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round(startsAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): RollingBrokerQuota {
        return RollingBrokerQuota(
            code = code,
            label = label,
            title = title,
            priority = priority,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling broker quota in status requested. */
fun newRollingBrokerQuota(code: String, label: String): RollingBrokerQuota {
    return RollingBrokerQuota(
        code = code,
        label = label,
        title = "",
        priority = 0,
        startsAt = 0,
        status = RollingBrokerQuotaStatus.REQUESTED,
    )
}
