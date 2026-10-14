package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default redelivery audit is in its life cycle. */
enum class DefaultRedeliveryAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default redelivery audit as the redelivery team keeps it. */
data class DefaultRedeliveryAudit(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var phone: String,
    var maximum: Double,
    var dock: String,
    var cost: Double,
    var startsAt: Int,
    var weight: Double,
    var routeCode: String,
    var checksum: String,
    var status: DefaultRedeliveryAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliveryAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliveryAuditStatus): Boolean {
        when (status) {
            DefaultRedeliveryAuditStatus.DRAFT -> return next == DefaultRedeliveryAuditStatus.ACTIVE || next == DefaultRedeliveryAuditStatus.CLOSED
            DefaultRedeliveryAuditStatus.PENDING -> {
                when (next) {
                    DefaultRedeliveryAuditStatus.SUSPENDED, DefaultRedeliveryAuditStatus.CLOSED -> return true
                }
                return false
            }
            DefaultRedeliveryAuditStatus.ACTIVE -> return next == DefaultRedeliveryAuditStatus.SUSPENDED
            DefaultRedeliveryAuditStatus.SUSPENDED -> {
                when (next) {
                    DefaultRedeliveryAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRedeliveryAuditStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery audit $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round((weight - maximum) * 100) / 100

    fun snapshot(): DefaultRedeliveryAudit {
        return DefaultRedeliveryAudit(
            code = code,
            label = label,
            distanceKm = distanceKm,
            phone = phone,
            maximum = maximum,
            dock = dock,
            cost = cost,
            startsAt = startsAt,
            weight = weight,
            routeCode = routeCode,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default redelivery audit in status draft. */
fun newDefaultRedeliveryAudit(code: String, label: String): DefaultRedeliveryAudit {
    return DefaultRedeliveryAudit(
        code = code,
        label = label,
        distanceKm = 0.0,
        phone = "",
        maximum = 0.0,
        dock = "",
        cost = 0.0,
        startsAt = 0,
        weight = 0.0,
        routeCode = "",
        checksum = "",
        status = DefaultRedeliveryAuditStatus.DRAFT,
    )
}
