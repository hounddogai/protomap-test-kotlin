package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive service plan record is in its life cycle. */
enum class AdaptiveServicePlanRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive service plan record and its current status. */
data class AdaptiveServicePlanRecord(
    var code: String,
    var name: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var distanceKm: Double,
    var channel: String,
    var reviewer: String,
    var title: String,
    var retries: Int,
    var status: AdaptiveServicePlanRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveServicePlanRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveServicePlanRecordStatus): Boolean {
        when (status) {
            AdaptiveServicePlanRecordStatus.DRAFT -> {
                when (next) {
                    AdaptiveServicePlanRecordStatus.SUSPENDED, AdaptiveServicePlanRecordStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveServicePlanRecordStatus.PENDING -> {
                when (next) {
                    AdaptiveServicePlanRecordStatus.ACTIVE -> return true
                }
                return false
            }
            AdaptiveServicePlanRecordStatus.ACTIVE -> {
                when (next) {
                    AdaptiveServicePlanRecordStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveServicePlanRecordStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveServicePlanRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveServicePlanRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive service plan record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedTotal(): Double = round(windowDays.toDouble() * distanceKm * 100) / 100

    fun snapshot(): AdaptiveServicePlanRecord {
        return AdaptiveServicePlanRecord(
            code = code,
            name = name,
            windowDays = windowDays,
            attributes = attributes,
            distanceKm = distanceKm,
            channel = channel,
            reviewer = reviewer,
            title = title,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive service plan record in status draft. */
fun newAdaptiveServicePlanRecord(code: String, name: String): AdaptiveServicePlanRecord {
    return AdaptiveServicePlanRecord(
        code = code,
        name = name,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        distanceKm = 0.0,
        channel = "",
        reviewer = "",
        title = "",
        retries = 0,
        status = AdaptiveServicePlanRecordStatus.DRAFT,
    )
}
