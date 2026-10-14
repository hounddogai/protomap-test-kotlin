package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary transit time notice records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a primary transit time notice is in its life cycle. */
enum class PrimaryTransitTimeNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary transit time notice as the transit time team keeps it. */
data class PrimaryTransitTimeNotice(
    var code: String,
    var label: String,
    var summary: String,
    var attempts: Int,
    var checksum: String,
    var createdAt: Int,
    var category: String,
    var updatedAt: Int,
    var region: String,
    var driverId: String,
    var budget: Double,
    var status: PrimaryTransitTimeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeNoticeStatus): Boolean {
        when (status) {
            PrimaryTransitTimeNoticeStatus.PLANNED -> return next == PrimaryTransitTimeNoticeStatus.IN_PROGRESS || next == PrimaryTransitTimeNoticeStatus.CANCELLED
            PrimaryTransitTimeNoticeStatus.SCHEDULED -> {
                when (next) {
                    PrimaryTransitTimeNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryTransitTimeNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryTransitTimeNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            PrimaryTransitTimeNoticeStatus.COMPLETED -> {
                when (next) {
                    PrimaryTransitTimeNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary transit time notice"} $code"

    fun weightedExposure(): Double = round(budget * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryTransitTimeNotice {
        return PrimaryTransitTimeNotice(
            code = code,
            label = label,
            summary = summary,
            attempts = attempts,
            checksum = checksum,
            createdAt = createdAt,
            category = category,
            updatedAt = updatedAt,
            region = region,
            driverId = driverId,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary transit time notice in status planned. */
fun newPrimaryTransitTimeNotice(code: String, label: String): PrimaryTransitTimeNotice {
    return PrimaryTransitTimeNotice(
        code = code,
        label = label,
        summary = "",
        attempts = 0,
        checksum = "",
        createdAt = 0,
        category = "",
        updatedAt = 0,
        region = "",
        driverId = "",
        budget = 0.0,
        status = PrimaryTransitTimeNoticeStatus.PLANNED,
    )
}
