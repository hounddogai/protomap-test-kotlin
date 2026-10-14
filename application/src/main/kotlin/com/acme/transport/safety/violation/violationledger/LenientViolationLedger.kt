package com.acme.transport.safety.violation.violationledger

import com.acme.transport.kit.text.normalize

// The lenient violation ledger records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a lenient violation ledger is in its life cycle. */
enum class LenientViolationLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient violation ledger. Its code is unique within a tenant. */
data class LenientViolationLedger(
    var code: String,
    var name: String,
    var version: Int,
    var country: String,
    var phone: String,
    var approved: Boolean,
    var status: LenientViolationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientViolationLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientViolationLedgerStatus): Boolean {
        when (status) {
            LenientViolationLedgerStatus.CREATED -> return next == LenientViolationLedgerStatus.VALIDATED || next == LenientViolationLedgerStatus.RETIRED
            LenientViolationLedgerStatus.VALIDATED -> return next == LenientViolationLedgerStatus.PUBLISHED || next == LenientViolationLedgerStatus.RETIRED
            LenientViolationLedgerStatus.PUBLISHED -> {
                when (next) {
                    LenientViolationLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientViolationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lenient violation ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"lenient violation ledger"} $code"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): LenientViolationLedger {
        return LenientViolationLedger(
            code = code,
            name = name,
            version = version,
            country = country,
            phone = phone,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient violation ledger in status created. */
fun newLenientViolationLedger(code: String, name: String): LenientViolationLedger {
    return LenientViolationLedger(
        code = code,
        name = name,
        version = 0,
        country = "",
        phone = "",
        approved = false,
        status = LenientViolationLedgerStatus.CREATED,
    )
}
