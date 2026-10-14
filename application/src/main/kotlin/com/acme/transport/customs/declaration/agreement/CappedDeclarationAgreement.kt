package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped declaration agreement records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a capped declaration agreement is in its life cycle. */
enum class CappedDeclarationAgreementStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped declaration agreement as the declaration team keeps it. */
data class CappedDeclarationAgreement(
    var code: String,
    var title: String,
    var graceDays: Int,
    var ceiling: Double,
    var sequence: Int,
    var routeCode: String,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var phone: String,
    var status: CappedDeclarationAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDeclarationAgreementStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDeclarationAgreementStatus): Boolean {
        when (status) {
            CappedDeclarationAgreementStatus.OPEN -> {
                when (next) {
                    CappedDeclarationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedDeclarationAgreementStatus.IN_REVIEW -> {
                when (next) {
                    CappedDeclarationAgreementStatus.RESOLVED -> return true
                }
                return false
            }
            CappedDeclarationAgreementStatus.RESOLVED -> {
                when (next) {
                    CappedDeclarationAgreementStatus.REOPENED, CappedDeclarationAgreementStatus.ARCHIVED -> return true
                }
                return false
            }
            CappedDeclarationAgreementStatus.REOPENED -> return next == CappedDeclarationAgreementStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedDeclarationAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped declaration agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped declaration agreement"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round(graceDays.toDouble() * ceiling * 100) / 100

    fun snapshot(): CappedDeclarationAgreement {
        return CappedDeclarationAgreement(
            code = code,
            title = title,
            graceDays = graceDays,
            ceiling = ceiling,
            sequence = sequence,
            routeCode = routeCode,
            odometer = odometer,
            attributes = attributes,
            notes = notes,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped declaration agreement in status open. */
fun newCappedDeclarationAgreement(code: String, title: String): CappedDeclarationAgreement {
    return CappedDeclarationAgreement(
        code = code,
        title = title,
        graceDays = 0,
        ceiling = 0.0,
        sequence = 0,
        routeCode = "",
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        phone = "",
        status = CappedDeclarationAgreementStatus.OPEN,
    )
}
