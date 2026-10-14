package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default audit notice is in its life cycle. */
enum class DefaultAuditNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default audit notice, with what the planners need to act on it. */
data class DefaultAuditNotice(
    var code: String,
    var title: String,
    var limit: Int,
    var vehicleId: String,
    var ratio: Double,
    var pinned: Boolean,
    var startsAt: Int,
    var summary: String,
    var pallets: Int,
    var odometer: Int,
    var driverId: String,
    var status: DefaultAuditNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAuditNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAuditNoticeStatus): Boolean {
        when (status) {
            DefaultAuditNoticeStatus.OPEN -> {
                when (next) {
                    DefaultAuditNoticeStatus.IN_REVIEW -> return true
                }
                return false
            }
            DefaultAuditNoticeStatus.IN_REVIEW -> {
                when (next) {
                    DefaultAuditNoticeStatus.RESOLVED, DefaultAuditNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultAuditNoticeStatus.RESOLVED -> return next == DefaultAuditNoticeStatus.REOPENED || next == DefaultAuditNoticeStatus.ARCHIVED
            DefaultAuditNoticeStatus.REOPENED -> {
                when (next) {
                    DefaultAuditNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultAuditNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default audit notice $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((pallets.toDouble() + ratio) * 100) / 100

    fun snapshot(): DefaultAuditNotice {
        return DefaultAuditNotice(
            code = code,
            title = title,
            limit = limit,
            vehicleId = vehicleId,
            ratio = ratio,
            pinned = pinned,
            startsAt = startsAt,
            summary = summary,
            pallets = pallets,
            odometer = odometer,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default audit notice in status open. */
fun newDefaultAuditNotice(code: String, title: String): DefaultAuditNotice {
    return DefaultAuditNotice(
        code = code,
        title = title,
        limit = 0,
        vehicleId = "",
        ratio = 0.0,
        pinned = false,
        startsAt = 0,
        summary = "",
        pallets = 0,
        odometer = 0,
        driverId = "",
        status = DefaultAuditNoticeStatus.OPEN,
    )
}
