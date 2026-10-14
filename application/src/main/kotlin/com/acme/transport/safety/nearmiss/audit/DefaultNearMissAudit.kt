package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default near miss audit records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a default near miss audit is in its life cycle. */
enum class DefaultNearMissAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default near miss audit, with what the planners need to act on it. */
data class DefaultNearMissAudit(
    var code: String,
    var title: String,
    var amount: Double,
    var externalRef: String,
    var pallets: Int,
    var currency: String,
    var floorValue: Double,
    var shiftCode: String,
    var ownerId: String,
    var source: String,
    var minimum: Double,
    var status: DefaultNearMissAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultNearMissAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultNearMissAuditStatus): Boolean {
        when (status) {
            DefaultNearMissAuditStatus.PLANNED -> {
                when (next) {
                    DefaultNearMissAuditStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultNearMissAuditStatus.SCHEDULED -> {
                when (next) {
                    DefaultNearMissAuditStatus.COMPLETED, DefaultNearMissAuditStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultNearMissAuditStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultNearMissAuditStatus.COMPLETED, DefaultNearMissAuditStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultNearMissAuditStatus.COMPLETED -> return next == DefaultNearMissAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultNearMissAuditStatus) {
        if (!canMoveTo(next)) {
            error("default near miss audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round((minimum - floorValue) * 100) / 100

    fun snapshot(): DefaultNearMissAudit {
        return DefaultNearMissAudit(
            code = code,
            title = title,
            amount = amount,
            externalRef = externalRef,
            pallets = pallets,
            currency = currency,
            floorValue = floorValue,
            shiftCode = shiftCode,
            ownerId = ownerId,
            source = source,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a default near miss audit in status planned. */
fun newDefaultNearMissAudit(code: String, title: String): DefaultNearMissAudit {
    return DefaultNearMissAudit(
        code = code,
        title = title,
        amount = 0.0,
        externalRef = "",
        pallets = 0,
        currency = "",
        floorValue = 0.0,
        shiftCode = "",
        ownerId = "",
        source = "",
        minimum = 0.0,
        status = DefaultNearMissAuditStatus.PLANNED,
    )
}
