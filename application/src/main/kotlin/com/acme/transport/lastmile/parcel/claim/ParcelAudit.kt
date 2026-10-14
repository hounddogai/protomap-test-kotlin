package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel audit is in its life cycle. */
enum class ParcelAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a parcel audit and its current status. */
data class ParcelAudit(
    var code: String,
    var title: String,
    var archived: Boolean,
    var channel: String,
    var active: Boolean,
    var driverId: String,
    var loadKg: Double,
    var dock: String,
    var unit: String,
    var createdAt: Int,
    var weight: Double,
    var status: ParcelAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelAuditStatus): Boolean {
        when (status) {
            ParcelAuditStatus.OPEN -> return next == ParcelAuditStatus.REOPENED || next == ParcelAuditStatus.ARCHIVED
            ParcelAuditStatus.IN_REVIEW -> return next == ParcelAuditStatus.RESOLVED
            ParcelAuditStatus.RESOLVED -> {
                when (next) {
                    ParcelAuditStatus.REOPENED -> return true
                }
                return false
            }
            ParcelAuditStatus.REOPENED -> {
                when (next) {
                    ParcelAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelAuditStatus) {
        if (!canMoveTo(next)) {
            error("parcel audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"parcel audit"} $code"

    fun adjustedExposure(): Double = round((weight + loadKg) * 100) / 100

    fun snapshot(): ParcelAudit {
        return ParcelAudit(
            code = code,
            title = title,
            archived = archived,
            channel = channel,
            active = active,
            driverId = driverId,
            loadKg = loadKg,
            dock = dock,
            unit = unit,
            createdAt = createdAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a parcel audit in status open. */
fun newParcelAudit(code: String, title: String): ParcelAudit {
    return ParcelAudit(
        code = code,
        title = title,
        archived = false,
        channel = "",
        active = false,
        driverId = "",
        loadKg = 0.0,
        dock = "",
        unit = "",
        createdAt = 0,
        weight = 0.0,
        status = ParcelAuditStatus.OPEN,
    )
}
