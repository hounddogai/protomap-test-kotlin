package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit override is in its life cycle. */
enum class AuditOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A audit override. Its code is unique within a tenant. */
data class AuditOverride(
    var code: String,
    var label: String,
    var limit: Int,
    var category: String,
    var tags: MutableList<String>,
    var odometer: Int,
    var country: String,
    var status: AuditOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditOverrideStatus): Boolean {
        when (status) {
            AuditOverrideStatus.DRAFT -> return next == AuditOverrideStatus.SUSPENDED || next == AuditOverrideStatus.CLOSED
            AuditOverrideStatus.PENDING -> {
                when (next) {
                    AuditOverrideStatus.SUSPENDED, AuditOverrideStatus.CLOSED -> return true
                }
                return false
            }
            AuditOverrideStatus.ACTIVE -> return next == AuditOverrideStatus.SUSPENDED || next == AuditOverrideStatus.CLOSED
            AuditOverrideStatus.SUSPENDED -> return next == AuditOverrideStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AuditOverrideStatus) {
        if (!canMoveTo(next)) {
            error("audit override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((limit.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): AuditOverride {
        return AuditOverride(
            code = code,
            label = label,
            limit = limit,
            category = category,
            tags = tags,
            odometer = odometer,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a audit override in status draft. */
fun newAuditOverride(code: String, label: String): AuditOverride {
    return AuditOverride(
        code = code,
        label = label,
        limit = 0,
        category = "",
        tags = mutableListOf<String>(),
        odometer = 0,
        country = "",
        status = AuditOverrideStatus.DRAFT,
    )
}
