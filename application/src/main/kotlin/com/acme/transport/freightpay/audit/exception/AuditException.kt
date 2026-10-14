package com.acme.transport.freightpay.audit.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit exception records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a audit exception is in its life cycle. */
enum class AuditExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One audit exception, with what the planners need to act on it. */
data class AuditException(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var contactEmail: String,
    var title: String,
    var createdAt: Int,
    var cost: Double,
    var durationMinutes: Int,
    var vehicleId: String,
    var expiresAt: Int,
    var maximum: Double,
    var status: AuditExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditExceptionStatus): Boolean {
        when (status) {
            AuditExceptionStatus.IDLE -> return next == AuditExceptionStatus.STOPPED
            AuditExceptionStatus.WARMING -> return next == AuditExceptionStatus.READY || next == AuditExceptionStatus.DRAINING
            AuditExceptionStatus.READY -> return next == AuditExceptionStatus.DRAINING || next == AuditExceptionStatus.STOPPED
            AuditExceptionStatus.DRAINING -> {
                when (next) {
                    AuditExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditExceptionStatus) {
        if (!canMoveTo(next)) {
            error("audit exception $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round((maximum - fuelLiters) * 100) / 100

    fun snapshot(): AuditException {
        return AuditException(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            contactEmail = contactEmail,
            title = title,
            createdAt = createdAt,
            cost = cost,
            durationMinutes = durationMinutes,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a audit exception in status idle. */
fun newAuditException(code: String, name: String): AuditException {
    return AuditException(
        code = code,
        name = name,
        fuelLiters = 0.0,
        contactEmail = "",
        title = "",
        createdAt = 0,
        cost = 0.0,
        durationMinutes = 0,
        vehicleId = "",
        expiresAt = 0,
        maximum = 0.0,
        status = AuditExceptionStatus.IDLE,
    )
}
