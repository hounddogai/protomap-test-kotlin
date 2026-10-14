package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize

// The audit notice records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit notice is in its life cycle. */
enum class AuditNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A audit notice as the audit team keeps it. */
data class AuditNotice(
    var code: String,
    var label: String,
    var source: String,
    var discount: Double,
    var channel: String,
    var status: AuditNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditNoticeStatus): Boolean {
        when (status) {
            AuditNoticeStatus.NEW -> {
                when (next) {
                    AuditNoticeStatus.RUNNING -> return true
                }
                return false
            }
            AuditNoticeStatus.QUEUED -> {
                when (next) {
                    AuditNoticeStatus.RUNNING, AuditNoticeStatus.DONE -> return true
                }
                return false
            }
            AuditNoticeStatus.RUNNING -> {
                when (next) {
                    AuditNoticeStatus.FAILED -> return true
                }
                return false
            }
            AuditNoticeStatus.DONE -> return next == AuditNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AuditNoticeStatus) {
        if (!canMoveTo(next)) {
            error("audit notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = discount

    fun snapshot(): AuditNotice {
        return AuditNotice(
            code = code,
            label = label,
            source = source,
            discount = discount,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a audit notice in status new. */
fun newAuditNotice(code: String, label: String): AuditNotice {
    return AuditNotice(
        code = code,
        label = label,
        source = "",
        discount = 0.0,
        channel = "",
        status = AuditNoticeStatus.NEW,
    )
}
