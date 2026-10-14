package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender audit is in its life cycle. */
enum class TenderAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tender audit as the tender team keeps it. */
data class TenderAudit(
    var code: String,
    var title: String,
    var amount: Double,
    var capacity: Int,
    var phone: String,
    var segment: String,
    var status: TenderAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderAuditStatus): Boolean {
        when (status) {
            TenderAuditStatus.NEW -> return next == TenderAuditStatus.QUEUED || next == TenderAuditStatus.FAILED
            TenderAuditStatus.QUEUED -> return next == TenderAuditStatus.RUNNING || next == TenderAuditStatus.DONE
            TenderAuditStatus.RUNNING -> {
                when (next) {
                    TenderAuditStatus.DONE, TenderAuditStatus.FAILED -> return true
                }
                return false
            }
            TenderAuditStatus.DONE -> return next == TenderAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TenderAuditStatus) {
        if (!canMoveTo(next)) {
            error("tender audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tender audit"} $code"

    fun weightedValue(): Double = round(amount * capacity.toDouble() * 100) / 100

    fun snapshot(): TenderAudit {
        return TenderAudit(
            code = code,
            title = title,
            amount = amount,
            capacity = capacity,
            phone = phone,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tender audit in status new. */
fun newTenderAudit(code: String, title: String): TenderAudit {
    return TenderAudit(
        code = code,
        title = title,
        amount = 0.0,
        capacity = 0,
        phone = "",
        segment = "",
        status = TenderAuditStatus.NEW,
    )
}
