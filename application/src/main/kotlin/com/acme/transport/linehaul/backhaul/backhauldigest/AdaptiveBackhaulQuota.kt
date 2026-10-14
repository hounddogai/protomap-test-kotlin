package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive backhaul quota is in its life cycle. */
enum class AdaptiveBackhaulQuotaStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive backhaul quota, with what the planners need to act on it. */
data class AdaptiveBackhaulQuota(
    var code: String,
    var name: String,
    var revision: Int,
    var weight: Double,
    var archived: Boolean,
    var timezone: String,
    var margin: Double,
    var graceDays: Int,
    var windowDays: Int,
    var visible: Boolean,
    var limit: Int,
    var status: AdaptiveBackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulQuotaStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulQuotaStatus): Boolean {
        when (status) {
            AdaptiveBackhaulQuotaStatus.PLANNED -> {
                when (next) {
                    AdaptiveBackhaulQuotaStatus.IN_PROGRESS -> return true
                }
                return false
            }
            AdaptiveBackhaulQuotaStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveBackhaulQuotaStatus.IN_PROGRESS, AdaptiveBackhaulQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveBackhaulQuotaStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveBackhaulQuotaStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveBackhaulQuotaStatus.COMPLETED -> {
                when (next) {
                    AdaptiveBackhaulQuotaStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul quota $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive backhaul quota"} $code"

    fun netExposure(): Double = round((margin + weight) * 100) / 100

    fun snapshot(): AdaptiveBackhaulQuota {
        return AdaptiveBackhaulQuota(
            code = code,
            name = name,
            revision = revision,
            weight = weight,
            archived = archived,
            timezone = timezone,
            margin = margin,
            graceDays = graceDays,
            windowDays = windowDays,
            visible = visible,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive backhaul quota in status planned. */
fun newAdaptiveBackhaulQuota(code: String, name: String): AdaptiveBackhaulQuota {
    return AdaptiveBackhaulQuota(
        code = code,
        name = name,
        revision = 0,
        weight = 0.0,
        archived = false,
        timezone = "",
        margin = 0.0,
        graceDays = 0,
        windowDays = 0,
        visible = false,
        limit = 0,
        status = AdaptiveBackhaulQuotaStatus.PLANNED,
    )
}
