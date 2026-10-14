package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer audit is in its life cycle. */
enum class TrailerAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a trailer audit and its current status. */
data class TrailerAudit(
    var code: String,
    var name: String,
    var graceDays: Int,
    var depot: String,
    var windowDays: Int,
    var externalRef: String,
    var quantity: Int,
    var createdAt: Int,
    var cost: Double,
    var score: Double,
    var status: TrailerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerAuditStatus): Boolean {
        when (status) {
            TrailerAuditStatus.DRAFT -> return next == TrailerAuditStatus.PENDING
            TrailerAuditStatus.PENDING -> {
                when (next) {
                    TrailerAuditStatus.ACTIVE, TrailerAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            TrailerAuditStatus.ACTIVE -> return next == TrailerAuditStatus.SUSPENDED || next == TrailerAuditStatus.CLOSED
            TrailerAuditStatus.SUSPENDED -> return next == TrailerAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TrailerAuditStatus) {
        if (!canMoveTo(next)) {
            error("trailer audit $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round((score - windowDays.toDouble()) * 100) / 100

    fun snapshot(): TrailerAudit {
        return TrailerAudit(
            code = code,
            name = name,
            graceDays = graceDays,
            depot = depot,
            windowDays = windowDays,
            externalRef = externalRef,
            quantity = quantity,
            createdAt = createdAt,
            cost = cost,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a trailer audit in status draft. */
fun newTrailerAudit(code: String, name: String): TrailerAudit {
    return TrailerAudit(
        code = code,
        name = name,
        graceDays = 0,
        depot = "",
        windowDays = 0,
        externalRef = "",
        quantity = 0,
        createdAt = 0,
        cost = 0.0,
        score = 0.0,
        status = TrailerAuditStatus.DRAFT,
    )
}
