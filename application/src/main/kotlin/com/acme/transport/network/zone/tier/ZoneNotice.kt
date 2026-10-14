package com.acme.transport.network.zone.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone notice is in its life cycle. */
enum class ZoneNoticeStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One zone notice, with what the planners need to act on it. */
data class ZoneNotice(
    var code: String,
    var title: String,
    var category: String,
    var ratio: Double,
    var budget: Double,
    var depot: String,
    var createdAt: Int,
    var updatedAt: Int,
    var status: ZoneNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneNoticeStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneNoticeStatus): Boolean {
        when (status) {
            ZoneNoticeStatus.DRAFT -> return next == ZoneNoticeStatus.PENDING || next == ZoneNoticeStatus.SUSPENDED
            ZoneNoticeStatus.PENDING -> return next == ZoneNoticeStatus.SUSPENDED
            ZoneNoticeStatus.ACTIVE -> return next == ZoneNoticeStatus.SUSPENDED || next == ZoneNoticeStatus.CLOSED
            ZoneNoticeStatus.SUSPENDED -> {
                when (next) {
                    ZoneNoticeStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneNoticeStatus) {
        if (!canMoveTo(next)) {
            error("zone notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round((budget + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ZoneNotice {
        return ZoneNotice(
            code = code,
            title = title,
            category = category,
            ratio = ratio,
            budget = budget,
            depot = depot,
            createdAt = createdAt,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a zone notice in status draft. */
fun newZoneNotice(code: String, title: String): ZoneNotice {
    return ZoneNotice(
        code = code,
        title = title,
        category = "",
        ratio = 0.0,
        budget = 0.0,
        depot = "",
        createdAt = 0,
        updatedAt = 0,
        status = ZoneNoticeStatus.DRAFT,
    )
}
