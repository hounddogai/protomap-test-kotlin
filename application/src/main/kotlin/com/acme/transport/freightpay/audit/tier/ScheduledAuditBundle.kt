package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled audit bundle is in its life cycle. */
enum class ScheduledAuditBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a scheduled audit bundle and its current status. */
data class ScheduledAuditBundle(
    var code: String,
    var name: String,
    var phone: String,
    var dock: String,
    var unit: String,
    var odometer: Int,
    var driverId: String,
    var batchSize: Int,
    var graceDays: Int,
    var routeCode: String,
    var status: ScheduledAuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAuditBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAuditBundleStatus): Boolean {
        when (status) {
            ScheduledAuditBundleStatus.DRAFT -> {
                when (next) {
                    ScheduledAuditBundleStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledAuditBundleStatus.PENDING -> return next == ScheduledAuditBundleStatus.SUSPENDED || next == ScheduledAuditBundleStatus.CLOSED
            ScheduledAuditBundleStatus.ACTIVE -> {
                when (next) {
                    ScheduledAuditBundleStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledAuditBundleStatus.SUSPENDED -> return next == ScheduledAuditBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledAuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled audit bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round(odometer.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledAuditBundle {
        return ScheduledAuditBundle(
            code = code,
            name = name,
            phone = phone,
            dock = dock,
            unit = unit,
            odometer = odometer,
            driverId = driverId,
            batchSize = batchSize,
            graceDays = graceDays,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled audit bundle in status draft. */
fun newScheduledAuditBundle(code: String, name: String): ScheduledAuditBundle {
    return ScheduledAuditBundle(
        code = code,
        name = name,
        phone = "",
        dock = "",
        unit = "",
        odometer = 0,
        driverId = "",
        batchSize = 0,
        graceDays = 0,
        routeCode = "",
        status = ScheduledAuditBundleStatus.DRAFT,
    )
}
