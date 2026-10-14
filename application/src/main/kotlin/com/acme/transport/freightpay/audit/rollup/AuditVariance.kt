package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a audit variance is in its life cycle. */
enum class AuditVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A audit variance as the audit team keeps it. */
data class AuditVariance(
    var code: String,
    var title: String,
    var carrier: String,
    var contactEmail: String,
    var currency: String,
    var sequence: Int,
    var version: Int,
    var minimum: Double,
    var durationMinutes: Int,
    var status: AuditVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditVarianceStatus): Boolean {
        when (status) {
            AuditVarianceStatus.DRAFT -> return next == AuditVarianceStatus.PENDING || next == AuditVarianceStatus.CLOSED
            AuditVarianceStatus.PENDING -> {
                when (next) {
                    AuditVarianceStatus.ACTIVE -> return true
                }
                return false
            }
            AuditVarianceStatus.ACTIVE -> {
                when (next) {
                    AuditVarianceStatus.SUSPENDED -> return true
                }
                return false
            }
            AuditVarianceStatus.SUSPENDED -> {
                when (next) {
                    AuditVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditVarianceStatus) {
        if (!canMoveTo(next)) {
            error("audit variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"audit variance"} $code"

    fun netValue(): Double = round((minimum - sequence.toDouble()) * 100) / 100

    fun snapshot(): AuditVariance {
        return AuditVariance(
            code = code,
            title = title,
            carrier = carrier,
            contactEmail = contactEmail,
            currency = currency,
            sequence = sequence,
            version = version,
            minimum = minimum,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit variance in status draft. */
fun newAuditVariance(code: String, title: String): AuditVariance {
    return AuditVariance(
        code = code,
        title = title,
        carrier = "",
        contactEmail = "",
        currency = "",
        sequence = 0,
        version = 0,
        minimum = 0.0,
        durationMinutes = 0,
        status = AuditVarianceStatus.DRAFT,
    )
}
