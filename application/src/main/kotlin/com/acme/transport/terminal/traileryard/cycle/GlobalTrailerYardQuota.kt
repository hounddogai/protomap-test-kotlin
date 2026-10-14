package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global trailer yard quota is in its life cycle. */
enum class GlobalTrailerYardQuotaStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global trailer yard quota as the trailer yard team keeps it. */
data class GlobalTrailerYardQuota(
    var code: String,
    var label: String,
    var attempts: Int,
    var version: Int,
    var cost: Double,
    var summary: String,
    var attributes: MutableMap<String, String>,
    var region: String,
    var expiresAt: Int,
    var floorValue: Double,
    var status: GlobalTrailerYardQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerYardQuotaStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerYardQuotaStatus): Boolean {
        when (status) {
            GlobalTrailerYardQuotaStatus.NEW -> {
                when (next) {
                    GlobalTrailerYardQuotaStatus.DONE -> return true
                }
                return false
            }
            GlobalTrailerYardQuotaStatus.QUEUED -> return next == GlobalTrailerYardQuotaStatus.DONE || next == GlobalTrailerYardQuotaStatus.FAILED
            GlobalTrailerYardQuotaStatus.RUNNING -> return next == GlobalTrailerYardQuotaStatus.FAILED
            GlobalTrailerYardQuotaStatus.DONE -> {
                when (next) {
                    GlobalTrailerYardQuotaStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTrailerYardQuotaStatus) {
        if (!canMoveTo(next)) {
            error("global trailer yard quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((cost + floorValue) * 100) / 100

    fun snapshot(): GlobalTrailerYardQuota {
        return GlobalTrailerYardQuota(
            code = code,
            label = label,
            attempts = attempts,
            version = version,
            cost = cost,
            summary = summary,
            attributes = attributes,
            region = region,
            expiresAt = expiresAt,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global trailer yard quota in status new. */
fun newGlobalTrailerYardQuota(code: String, label: String): GlobalTrailerYardQuota {
    return GlobalTrailerYardQuota(
        code = code,
        label = label,
        attempts = 0,
        version = 0,
        cost = 0.0,
        summary = "",
        attributes = mutableMapOf<String, String>(),
        region = "",
        expiresAt = 0,
        floorValue = 0.0,
        status = GlobalTrailerYardQuotaStatus.NEW,
    )
}
