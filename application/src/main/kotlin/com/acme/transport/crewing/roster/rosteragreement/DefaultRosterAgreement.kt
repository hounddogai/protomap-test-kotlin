package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default roster agreement records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a default roster agreement is in its life cycle. */
enum class DefaultRosterAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default roster agreement. Its code is unique within a tenant. */
data class DefaultRosterAgreement(
    var code: String,
    var label: String,
    var lane: String,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var stopCount: Int,
    var minimum: Double,
    var contactEmail: String,
    var unit: String,
    var shiftCode: String,
    var endsAt: Int,
    var status: DefaultRosterAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterAgreementStatus): Boolean {
        when (status) {
            DefaultRosterAgreementStatus.DRAFT -> return next == DefaultRosterAgreementStatus.PENDING
            DefaultRosterAgreementStatus.PENDING -> {
                when (next) {
                    DefaultRosterAgreementStatus.ACTIVE, DefaultRosterAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            DefaultRosterAgreementStatus.ACTIVE -> {
                when (next) {
                    DefaultRosterAgreementStatus.CLOSED -> return true
                }
                return false
            }
            DefaultRosterAgreementStatus.SUSPENDED -> {
                when (next) {
                    DefaultRosterAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRosterAgreementStatus) {
        if (!canMoveTo(next)) {
            error("default roster agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((minimum + batchSize.toDouble()) * 100) / 100

    fun snapshot(): DefaultRosterAgreement {
        return DefaultRosterAgreement(
            code = code,
            label = label,
            lane = lane,
            batchSize = batchSize,
            attributes = attributes,
            stopCount = stopCount,
            minimum = minimum,
            contactEmail = contactEmail,
            unit = unit,
            shiftCode = shiftCode,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default roster agreement in status draft. */
fun newDefaultRosterAgreement(code: String, label: String): DefaultRosterAgreement {
    return DefaultRosterAgreement(
        code = code,
        label = label,
        lane = "",
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        stopCount = 0,
        minimum = 0.0,
        contactEmail = "",
        unit = "",
        shiftCode = "",
        endsAt = 0,
        status = DefaultRosterAgreementStatus.DRAFT,
    )
}
