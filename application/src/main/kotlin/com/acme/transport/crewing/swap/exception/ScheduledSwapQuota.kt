package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled swap quota is in its life cycle. */
enum class ScheduledSwapQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled swap quota as the swap team keeps it. */
data class ScheduledSwapQuota(
    var code: String,
    var title: String,
    var notes: String,
    var maximum: Double,
    var routeCode: String,
    var attributes: MutableMap<String, String>,
    var archived: Boolean,
    var unit: String,
    var updatedAt: Int,
    var status: ScheduledSwapQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSwapQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSwapQuotaStatus): Boolean {
        when (status) {
            ScheduledSwapQuotaStatus.REQUESTED -> {
                when (next) {
                    ScheduledSwapQuotaStatus.REVOKED, ScheduledSwapQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledSwapQuotaStatus.GRANTED -> return next == ScheduledSwapQuotaStatus.REVOKED || next == ScheduledSwapQuotaStatus.EXPIRED
            ScheduledSwapQuotaStatus.REVOKED -> {
                when (next) {
                    ScheduledSwapQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSwapQuotaStatus) {
        if (!canMoveTo(next)) {
            error("scheduled swap quota $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled swap quota"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round((maximum + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledSwapQuota {
        return ScheduledSwapQuota(
            code = code,
            title = title,
            notes = notes,
            maximum = maximum,
            routeCode = routeCode,
            attributes = attributes,
            archived = archived,
            unit = unit,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled swap quota in status requested. */
fun newScheduledSwapQuota(code: String, title: String): ScheduledSwapQuota {
    return ScheduledSwapQuota(
        code = code,
        title = title,
        notes = "",
        maximum = 0.0,
        routeCode = "",
        attributes = mutableMapOf<String, String>(),
        archived = false,
        unit = "",
        updatedAt = 0,
        status = ScheduledSwapQuotaStatus.REQUESTED,
    )
}
