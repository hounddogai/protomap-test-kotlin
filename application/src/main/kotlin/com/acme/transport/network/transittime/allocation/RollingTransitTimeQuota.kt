package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling transit time quota is in its life cycle. */
enum class RollingTransitTimeQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling transit time quota as the transit time team keeps it. */
data class RollingTransitTimeQuota(
    var code: String,
    var name: String,
    var version: Int,
    var margin: Double,
    var driverId: String,
    var cost: Double,
    var floorValue: Double,
    var attributes: MutableMap<String, String>,
    var threshold: Double,
    var status: RollingTransitTimeQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTransitTimeQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTransitTimeQuotaStatus): Boolean {
        when (status) {
            RollingTransitTimeQuotaStatus.NEW -> {
                when (next) {
                    RollingTransitTimeQuotaStatus.RUNNING -> return true
                }
                return false
            }
            RollingTransitTimeQuotaStatus.QUEUED -> return next == RollingTransitTimeQuotaStatus.RUNNING || next == RollingTransitTimeQuotaStatus.DONE
            RollingTransitTimeQuotaStatus.RUNNING -> {
                when (next) {
                    RollingTransitTimeQuotaStatus.DONE, RollingTransitTimeQuotaStatus.FAILED -> return true
                }
                return false
            }
            RollingTransitTimeQuotaStatus.DONE -> {
                when (next) {
                    RollingTransitTimeQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTransitTimeQuotaStatus) {
        if (!canMoveTo(next)) {
            error("rolling transit time quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"rolling transit time quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((threshold + cost) * 100) / 100

    fun snapshot(): RollingTransitTimeQuota {
        return RollingTransitTimeQuota(
            code = code,
            name = name,
            version = version,
            margin = margin,
            driverId = driverId,
            cost = cost,
            floorValue = floorValue,
            attributes = attributes,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling transit time quota in status new. */
fun newRollingTransitTimeQuota(code: String, name: String): RollingTransitTimeQuota {
    return RollingTransitTimeQuota(
        code = code,
        name = name,
        version = 0,
        margin = 0.0,
        driverId = "",
        cost = 0.0,
        floorValue = 0.0,
        attributes = mutableMapOf<String, String>(),
        threshold = 0.0,
        status = RollingTransitTimeQuotaStatus.NEW,
    )
}
