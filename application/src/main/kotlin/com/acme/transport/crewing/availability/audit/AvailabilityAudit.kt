package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability audit records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability audit is in its life cycle. */
enum class AvailabilityAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A availability audit. Its code is unique within a tenant. */
data class AvailabilityAudit(
    var code: String,
    var name: String,
    var phone: String,
    var trailerId: String,
    var stopCount: Int,
    var locale: String,
    var ownerId: String,
    var ratio: Double,
    var summary: String,
    var contactEmail: String,
    var status: AvailabilityAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityAuditStatus): Boolean {
        when (status) {
            AvailabilityAuditStatus.DRAFT -> return next == AvailabilityAuditStatus.ACTIVE || next == AvailabilityAuditStatus.SUSPENDED
            AvailabilityAuditStatus.PENDING -> {
                when (next) {
                    AvailabilityAuditStatus.ACTIVE, AvailabilityAuditStatus.CLOSED -> return true
                }
                return false
            }
            AvailabilityAuditStatus.ACTIVE -> return next == AvailabilityAuditStatus.CLOSED
            AvailabilityAuditStatus.SUSPENDED -> {
                when (next) {
                    AvailabilityAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityAuditStatus) {
        if (!canMoveTo(next)) {
            error("availability audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round((ratio + stopCount.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityAudit {
        return AvailabilityAudit(
            code = code,
            name = name,
            phone = phone,
            trailerId = trailerId,
            stopCount = stopCount,
            locale = locale,
            ownerId = ownerId,
            ratio = ratio,
            summary = summary,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a availability audit in status draft. */
fun newAvailabilityAudit(code: String, name: String): AvailabilityAudit {
    return AvailabilityAudit(
        code = code,
        name = name,
        phone = "",
        trailerId = "",
        stopCount = 0,
        locale = "",
        ownerId = "",
        ratio = 0.0,
        summary = "",
        contactEmail = "",
        status = AvailabilityAuditStatus.DRAFT,
    )
}
