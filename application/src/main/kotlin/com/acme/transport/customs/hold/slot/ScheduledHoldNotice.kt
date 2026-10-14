package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hold notice is in its life cycle. */
enum class ScheduledHoldNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled hold notice as the hold team keeps it. */
data class ScheduledHoldNotice(
    var code: String,
    var name: String,
    var channel: String,
    var createdAt: Int,
    var margin: Double,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var odometer: Int,
    var reviewer: String,
    var revision: Int,
    var status: ScheduledHoldNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldNoticeStatus): Boolean {
        when (status) {
            ScheduledHoldNoticeStatus.PLANNED -> {
                when (next) {
                    ScheduledHoldNoticeStatus.IN_PROGRESS, ScheduledHoldNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledHoldNoticeStatus.SCHEDULED -> {
                when (next) {
                    ScheduledHoldNoticeStatus.IN_PROGRESS, ScheduledHoldNoticeStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledHoldNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledHoldNoticeStatus.COMPLETED, ScheduledHoldNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledHoldNoticeStatus.COMPLETED -> {
                when (next) {
                    ScheduledHoldNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((revision.toDouble() + margin) * 100) / 100

    fun snapshot(): ScheduledHoldNotice {
        return ScheduledHoldNotice(
            code = code,
            name = name,
            channel = channel,
            createdAt = createdAt,
            margin = margin,
            windowDays = windowDays,
            attributes = attributes,
            unit = unit,
            odometer = odometer,
            reviewer = reviewer,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled hold notice in status planned. */
fun newScheduledHoldNotice(code: String, name: String): ScheduledHoldNotice {
    return ScheduledHoldNotice(
        code = code,
        name = name,
        channel = "",
        createdAt = 0,
        margin = 0.0,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        unit = "",
        odometer = 0,
        reviewer = "",
        revision = 0,
        status = ScheduledHoldNoticeStatus.PLANNED,
    )
}
