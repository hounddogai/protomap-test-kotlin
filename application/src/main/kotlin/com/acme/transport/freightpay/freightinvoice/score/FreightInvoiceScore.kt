package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The freight invoice score records of freightpay's freight invoice work, and the rules, stores, and
// calculations around them.

/** Where a freight invoice score is in its life cycle. */
enum class FreightInvoiceScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One freight invoice score, with what the planners need to act on it. */
data class FreightInvoiceScore(
    var code: String,
    var name: String,
    var budget: Double,
    var approved: Boolean,
    var score: Double,
    var windowDays: Int,
    var weight: Double,
    var dock: String,
    var reviewer: String,
    var status: FreightInvoiceScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceScoreStatus): Boolean {
        when (status) {
            FreightInvoiceScoreStatus.REQUESTED -> {
                when (next) {
                    FreightInvoiceScoreStatus.GRANTED -> return true
                }
                return false
            }
            FreightInvoiceScoreStatus.GRANTED -> return next == FreightInvoiceScoreStatus.REVOKED
            FreightInvoiceScoreStatus.REVOKED -> {
                when (next) {
                    FreightInvoiceScoreStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceScoreStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((weight - budget) * 100) / 100

    fun snapshot(): FreightInvoiceScore {
        return FreightInvoiceScore(
            code = code,
            name = name,
            budget = budget,
            approved = approved,
            score = score,
            windowDays = windowDays,
            weight = weight,
            dock = dock,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice score in status requested. */
fun newFreightInvoiceScore(code: String, name: String): FreightInvoiceScore {
    return FreightInvoiceScore(
        code = code,
        name = name,
        budget = 0.0,
        approved = false,
        score = 0.0,
        windowDays = 0,
        weight = 0.0,
        dock = "",
        reviewer = "",
        status = FreightInvoiceScoreStatus.REQUESTED,
    )
}
