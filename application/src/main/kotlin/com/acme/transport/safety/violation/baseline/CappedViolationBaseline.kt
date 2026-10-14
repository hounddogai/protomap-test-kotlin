package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped violation baseline records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a capped violation baseline is in its life cycle. */
enum class CappedViolationBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a capped violation baseline and its current status. */
data class CappedViolationBaseline(
    var code: String,
    var name: String,
    var title: String,
    var phone: String,
    var country: String,
    var trailerId: String,
    var createdAt: Int,
    var graceDays: Int,
    var routeCode: String,
    var currency: String,
    var shiftCode: String,
    var status: CappedViolationBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedViolationBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedViolationBaselineStatus): Boolean {
        when (status) {
            CappedViolationBaselineStatus.DRAFT -> return next == CappedViolationBaselineStatus.PENDING || next == CappedViolationBaselineStatus.ACTIVE
            CappedViolationBaselineStatus.PENDING -> return next == CappedViolationBaselineStatus.SUSPENDED || next == CappedViolationBaselineStatus.CLOSED
            CappedViolationBaselineStatus.ACTIVE -> return next == CappedViolationBaselineStatus.SUSPENDED
            CappedViolationBaselineStatus.SUSPENDED -> {
                when (next) {
                    CappedViolationBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedViolationBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped violation baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round(graceDays.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): CappedViolationBaseline {
        return CappedViolationBaseline(
            code = code,
            name = name,
            title = title,
            phone = phone,
            country = country,
            trailerId = trailerId,
            createdAt = createdAt,
            graceDays = graceDays,
            routeCode = routeCode,
            currency = currency,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped violation baseline in status draft. */
fun newCappedViolationBaseline(code: String, name: String): CappedViolationBaseline {
    return CappedViolationBaseline(
        code = code,
        name = name,
        title = "",
        phone = "",
        country = "",
        trailerId = "",
        createdAt = 0,
        graceDays = 0,
        routeCode = "",
        currency = "",
        shiftCode = "",
        status = CappedViolationBaselineStatus.DRAFT,
    )
}
