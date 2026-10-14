package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual trailer yard agreement records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a manual trailer yard agreement is in its life cycle. */
enum class ManualTrailerYardAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual trailer yard agreement. Its code is unique within a tenant. */
data class ManualTrailerYardAgreement(
    var code: String,
    var name: String,
    var notes: String,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var label: String,
    var durationMinutes: Int,
    var status: ManualTrailerYardAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardAgreementStatus): Boolean {
        when (status) {
            ManualTrailerYardAgreementStatus.PLANNED -> {
                when (next) {
                    ManualTrailerYardAgreementStatus.SCHEDULED, ManualTrailerYardAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            ManualTrailerYardAgreementStatus.SCHEDULED -> return next == ManualTrailerYardAgreementStatus.COMPLETED
            ManualTrailerYardAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    ManualTrailerYardAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            ManualTrailerYardAgreementStatus.COMPLETED -> {
                when (next) {
                    ManualTrailerYardAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardAgreementStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netVolume(): Double = round(durationMinutes.toDouble() * amount * 100) / 100

    fun snapshot(): ManualTrailerYardAgreement {
        return ManualTrailerYardAgreement(
            code = code,
            name = name,
            notes = notes,
            amount = amount,
            attributes = attributes,
            label = label,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual trailer yard agreement in status planned. */
fun newManualTrailerYardAgreement(code: String, name: String): ManualTrailerYardAgreement {
    return ManualTrailerYardAgreement(
        code = code,
        name = name,
        notes = "",
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        label = "",
        durationMinutes = 0,
        status = ManualTrailerYardAgreementStatus.PLANNED,
    )
}
