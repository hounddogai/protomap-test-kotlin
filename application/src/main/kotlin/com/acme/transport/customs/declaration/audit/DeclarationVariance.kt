package com.acme.transport.customs.declaration.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration variance is in its life cycle. */
enum class DeclarationVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A declaration variance. Its code is unique within a tenant. */
data class DeclarationVariance(
    var code: String,
    var title: String,
    var phone: String,
    var attempts: Int,
    var sequence: Int,
    var pinned: Boolean,
    var version: Int,
    var status: DeclarationVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationVarianceStatus): Boolean {
        when (status) {
            DeclarationVarianceStatus.REQUESTED -> {
                when (next) {
                    DeclarationVarianceStatus.GRANTED -> return true
                }
                return false
            }
            DeclarationVarianceStatus.GRANTED -> return next == DeclarationVarianceStatus.EXPIRED
            DeclarationVarianceStatus.REVOKED -> return next == DeclarationVarianceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DeclarationVarianceStatus) {
        if (!canMoveTo(next)) {
            error("declaration variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round(attempts.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): DeclarationVariance {
        return DeclarationVariance(
            code = code,
            title = title,
            phone = phone,
            attempts = attempts,
            sequence = sequence,
            pinned = pinned,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a declaration variance in status requested. */
fun newDeclarationVariance(code: String, title: String): DeclarationVariance {
    return DeclarationVariance(
        code = code,
        title = title,
        phone = "",
        attempts = 0,
        sequence = 0,
        pinned = false,
        version = 0,
        status = DeclarationVarianceStatus.REQUESTED,
    )
}
