package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone audit is in its life cycle. */
enum class ZoneAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A zone audit. Its code is unique within a tenant. */
data class ZoneAudit(
    var code: String,
    var name: String,
    var priority: Int,
    var locale: String,
    var currency: String,
    var revision: Int,
    var driverId: String,
    var ratio: Double,
    var status: ZoneAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneAuditStatus): Boolean {
        when (status) {
            ZoneAuditStatus.DRAFT -> return next == ZoneAuditStatus.SUSPENDED || next == ZoneAuditStatus.CLOSED
            ZoneAuditStatus.PENDING -> {
                when (next) {
                    ZoneAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneAuditStatus.ACTIVE -> {
                when (next) {
                    ZoneAuditStatus.SUSPENDED, ZoneAuditStatus.CLOSED -> return true
                }
                return false
            }
            ZoneAuditStatus.SUSPENDED -> return next == ZoneAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ZoneAuditStatus) {
        if (!canMoveTo(next)) {
            error("zone audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"zone audit"} $code"

    fun weightedValue(): Double = round((revision.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): ZoneAudit {
        return ZoneAudit(
            code = code,
            name = name,
            priority = priority,
            locale = locale,
            currency = currency,
            revision = revision,
            driverId = driverId,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a zone audit in status draft. */
fun newZoneAudit(code: String, name: String): ZoneAudit {
    return ZoneAudit(
        code = code,
        name = name,
        priority = 0,
        locale = "",
        currency = "",
        revision = 0,
        driverId = "",
        ratio = 0.0,
        status = ZoneAuditStatus.DRAFT,
    )
}
