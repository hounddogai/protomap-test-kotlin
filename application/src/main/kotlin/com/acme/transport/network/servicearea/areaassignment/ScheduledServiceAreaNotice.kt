package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled service area notice is in its life cycle. */
enum class ScheduledServiceAreaNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled service area notice, with what the planners need to act on it. */
data class ScheduledServiceAreaNotice(
    var code: String,
    var name: String,
    var weight: Double,
    var updatedAt: Int,
    var attributes: MutableMap<String, String>,
    var limit: Int,
    var score: Double,
    var country: String,
    var locale: String,
    var pinned: Boolean,
    var visible: Boolean,
    var status: ScheduledServiceAreaNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServiceAreaNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServiceAreaNoticeStatus): Boolean {
        when (status) {
            ScheduledServiceAreaNoticeStatus.IDLE -> {
                when (next) {
                    ScheduledServiceAreaNoticeStatus.WARMING, ScheduledServiceAreaNoticeStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledServiceAreaNoticeStatus.WARMING -> {
                when (next) {
                    ScheduledServiceAreaNoticeStatus.READY, ScheduledServiceAreaNoticeStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledServiceAreaNoticeStatus.READY -> return next == ScheduledServiceAreaNoticeStatus.DRAINING || next == ScheduledServiceAreaNoticeStatus.STOPPED
            ScheduledServiceAreaNoticeStatus.DRAINING -> {
                when (next) {
                    ScheduledServiceAreaNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledServiceAreaNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service area notice $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedLoad(): Double = round((weight + score) * 100) / 100

    fun snapshot(): ScheduledServiceAreaNotice {
        return ScheduledServiceAreaNotice(
            code = code,
            name = name,
            weight = weight,
            updatedAt = updatedAt,
            attributes = attributes,
            limit = limit,
            score = score,
            country = country,
            locale = locale,
            pinned = pinned,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled service area notice in status idle. */
fun newScheduledServiceAreaNotice(code: String, name: String): ScheduledServiceAreaNotice {
    return ScheduledServiceAreaNotice(
        code = code,
        name = name,
        weight = 0.0,
        updatedAt = 0,
        attributes = mutableMapOf<String, String>(),
        limit = 0,
        score = 0.0,
        country = "",
        locale = "",
        pinned = false,
        visible = false,
        status = ScheduledServiceAreaNoticeStatus.IDLE,
    )
}
