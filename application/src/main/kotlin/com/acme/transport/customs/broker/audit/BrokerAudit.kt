package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.text.normalize

// The broker audit records of customs's broker work, and the rules, stores, and calculations around them.

/** Where a broker audit is in its life cycle. */
enum class BrokerAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One broker audit, with what the planners need to act on it. */
data class BrokerAudit(
    var code: String,
    var title: String,
    var budget: Double,
    var currency: String,
    var summary: String,
    var status: BrokerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerAuditStatus): Boolean {
        when (status) {
            BrokerAuditStatus.NEW -> return next == BrokerAuditStatus.QUEUED
            BrokerAuditStatus.QUEUED -> return next == BrokerAuditStatus.RUNNING || next == BrokerAuditStatus.DONE
            BrokerAuditStatus.RUNNING -> return next == BrokerAuditStatus.DONE || next == BrokerAuditStatus.FAILED
            BrokerAuditStatus.DONE -> return next == BrokerAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BrokerAuditStatus) {
        if (!canMoveTo(next)) {
            error("broker audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = budget

    fun snapshot(): BrokerAudit {
        return BrokerAudit(
            code = code,
            title = title,
            budget = budget,
            currency = currency,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker audit in status new. */
fun newBrokerAudit(code: String, title: String): BrokerAudit {
    return BrokerAudit(
        code = code,
        title = title,
        budget = 0.0,
        currency = "",
        summary = "",
        status = BrokerAuditStatus.NEW,
    )
}
