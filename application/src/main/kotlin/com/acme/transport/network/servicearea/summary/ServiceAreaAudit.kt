package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area audit is in its life cycle. */
enum class ServiceAreaAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A service area audit as the service area team keeps it. */
data class ServiceAreaAudit(
    var code: String,
    var name: String,
    var minimum: Double,
    var currency: String,
    var priority: Int,
    var sequence: Int,
    var status: ServiceAreaAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaAuditStatus): Boolean {
        when (status) {
            ServiceAreaAuditStatus.OPEN -> return next == ServiceAreaAuditStatus.IN_REVIEW || next == ServiceAreaAuditStatus.RESOLVED
            ServiceAreaAuditStatus.IN_REVIEW -> {
                when (next) {
                    ServiceAreaAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaAuditStatus.RESOLVED -> {
                when (next) {
                    ServiceAreaAuditStatus.REOPENED, ServiceAreaAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            ServiceAreaAuditStatus.REOPENED -> {
                when (next) {
                    ServiceAreaAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaAuditStatus) {
        if (!canMoveTo(next)) {
            error("service area audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedValue(): Double = round(sequence.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ServiceAreaAudit {
        return ServiceAreaAudit(
            code = code,
            name = name,
            minimum = minimum,
            currency = currency,
            priority = priority,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a service area audit in status open. */
fun newServiceAreaAudit(code: String, name: String): ServiceAreaAudit {
    return ServiceAreaAudit(
        code = code,
        name = name,
        minimum = 0.0,
        currency = "",
        priority = 0,
        sequence = 0,
        status = ServiceAreaAuditStatus.OPEN,
    )
}
