package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient near miss agreement is in its life cycle. */
enum class LenientNearMissAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient near miss agreement. Its code is unique within a tenant. */
data class LenientNearMissAgreement(
    var code: String,
    var name: String,
    var unit: String,
    var updatedAt: Int,
    var minimum: Double,
    var description: String,
    var attributes: MutableMap<String, String>,
    var status: LenientNearMissAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissAgreementStatus): Boolean {
        when (status) {
            LenientNearMissAgreementStatus.DRAFT -> return next == LenientNearMissAgreementStatus.PENDING || next == LenientNearMissAgreementStatus.CLOSED
            LenientNearMissAgreementStatus.PENDING -> {
                when (next) {
                    LenientNearMissAgreementStatus.CLOSED -> return true
                }
                return false
            }
            LenientNearMissAgreementStatus.ACTIVE -> return next == LenientNearMissAgreementStatus.SUSPENDED || next == LenientNearMissAgreementStatus.CLOSED
            LenientNearMissAgreementStatus.SUSPENDED -> return next == LenientNearMissAgreementStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientNearMissAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient near miss agreement"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(minimum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LenientNearMissAgreement {
        return LenientNearMissAgreement(
            code = code,
            name = name,
            unit = unit,
            updatedAt = updatedAt,
            minimum = minimum,
            description = description,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient near miss agreement in status draft. */
fun newLenientNearMissAgreement(code: String, name: String): LenientNearMissAgreement {
    return LenientNearMissAgreement(
        code = code,
        name = name,
        unit = "",
        updatedAt = 0,
        minimum = 0.0,
        description = "",
        attributes = mutableMapOf<String, String>(),
        status = LenientNearMissAgreementStatus.DRAFT,
    )
}
