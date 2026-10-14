package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive cutoff record records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a adaptive cutoff record is in its life cycle. */
enum class AdaptiveCutoffRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive cutoff record and its current status. */
data class AdaptiveCutoffRecord(
    var code: String,
    var title: String,
    var windowDays: Int,
    var limit: Int,
    var description: String,
    var country: String,
    var region: String,
    var capacity: Int,
    var weight: Double,
    var reviewer: String,
    var distanceKm: Double,
    var status: AdaptiveCutoffRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCutoffRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCutoffRecordStatus): Boolean {
        when (status) {
            AdaptiveCutoffRecordStatus.OPEN -> {
                when (next) {
                    AdaptiveCutoffRecordStatus.IN_REVIEW -> return true
                }
                return false
            }
            AdaptiveCutoffRecordStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveCutoffRecordStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveCutoffRecordStatus.RESOLVED -> return next == AdaptiveCutoffRecordStatus.REOPENED
            AdaptiveCutoffRecordStatus.REOPENED -> {
                when (next) {
                    AdaptiveCutoffRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCutoffRecordStatus) {
        if (!canMoveTo(next)) {
            error("adaptive cutoff record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((capacity.toDouble() - weight) * 100) / 100

    fun snapshot(): AdaptiveCutoffRecord {
        return AdaptiveCutoffRecord(
            code = code,
            title = title,
            windowDays = windowDays,
            limit = limit,
            description = description,
            country = country,
            region = region,
            capacity = capacity,
            weight = weight,
            reviewer = reviewer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive cutoff record in status open. */
fun newAdaptiveCutoffRecord(code: String, title: String): AdaptiveCutoffRecord {
    return AdaptiveCutoffRecord(
        code = code,
        title = title,
        windowDays = 0,
        limit = 0,
        description = "",
        country = "",
        region = "",
        capacity = 0,
        weight = 0.0,
        reviewer = "",
        distanceKm = 0.0,
        status = AdaptiveCutoffRecordStatus.OPEN,
    )
}
