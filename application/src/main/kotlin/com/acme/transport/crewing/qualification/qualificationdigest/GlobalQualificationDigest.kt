package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global qualification digest records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a global qualification digest is in its life cycle. */
enum class GlobalQualificationDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global qualification digest, with what the planners need to act on it. */
data class GlobalQualificationDigest(
    var code: String,
    var name: String,
    var odometer: Int,
    var expiresAt: Int,
    var country: String,
    var updatedAt: Int,
    var graceDays: Int,
    var quantity: Int,
    var currency: String,
    var status: GlobalQualificationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalQualificationDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalQualificationDigestStatus): Boolean {
        when (status) {
            GlobalQualificationDigestStatus.DRAFT -> return next == GlobalQualificationDigestStatus.PENDING || next == GlobalQualificationDigestStatus.CLOSED
            GlobalQualificationDigestStatus.PENDING -> return next == GlobalQualificationDigestStatus.CLOSED
            GlobalQualificationDigestStatus.ACTIVE -> {
                when (next) {
                    GlobalQualificationDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalQualificationDigestStatus.SUSPENDED -> {
                when (next) {
                    GlobalQualificationDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalQualificationDigestStatus) {
        if (!canMoveTo(next)) {
            error("global qualification digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(updatedAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalQualificationDigest {
        return GlobalQualificationDigest(
            code = code,
            name = name,
            odometer = odometer,
            expiresAt = expiresAt,
            country = country,
            updatedAt = updatedAt,
            graceDays = graceDays,
            quantity = quantity,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global qualification digest in status draft. */
fun newGlobalQualificationDigest(code: String, name: String): GlobalQualificationDigest {
    return GlobalQualificationDigest(
        code = code,
        name = name,
        odometer = 0,
        expiresAt = 0,
        country = "",
        updatedAt = 0,
        graceDays = 0,
        quantity = 0,
        currency = "",
        status = GlobalQualificationDigestStatus.DRAFT,
    )
}
