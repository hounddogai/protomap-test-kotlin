package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.text.normalize

/** Where a lenient drop agreement is in its life cycle. */
enum class LenientDropAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient drop agreement as the drop team keeps it. */
data class LenientDropAgreement(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var windowDays: Int,
    var summary: String,
    var unit: String,
    var status: LenientDropAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDropAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDropAgreementStatus): Boolean {
        when (status) {
            LenientDropAgreementStatus.DRAFT -> {
                when (next) {
                    LenientDropAgreementStatus.CLOSED -> return true
                }
                return false
            }
            LenientDropAgreementStatus.PENDING -> return next == LenientDropAgreementStatus.ACTIVE || next == LenientDropAgreementStatus.CLOSED
            LenientDropAgreementStatus.ACTIVE -> {
                when (next) {
                    LenientDropAgreementStatus.SUSPENDED, LenientDropAgreementStatus.CLOSED -> return true
                }
                return false
            }
            LenientDropAgreementStatus.SUSPENDED -> {
                when (next) {
                    LenientDropAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientDropAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient drop agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): LenientDropAgreement {
        return LenientDropAgreement(
            code = code,
            label = label,
            attributes = attributes,
            windowDays = windowDays,
            summary = summary,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient drop agreement in status draft. */
fun newLenientDropAgreement(code: String, label: String): LenientDropAgreement {
    return LenientDropAgreement(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        windowDays = 0,
        summary = "",
        unit = "",
        status = LenientDropAgreementStatus.DRAFT,
    )
}
