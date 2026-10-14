package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.text.normalize

/** Where a tender quota is in its life cycle. */
enum class TenderQuotaStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tender quota and its current status. */
data class TenderQuota(
    var code: String,
    var label: String,
    var phone: String,
    var reviewer: String,
    var ownerId: String,
    var batchSize: Int,
    var status: TenderQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderQuotaStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderQuotaStatus): Boolean {
        when (status) {
            TenderQuotaStatus.IDLE -> return next == TenderQuotaStatus.READY || next == TenderQuotaStatus.STOPPED
            TenderQuotaStatus.WARMING -> {
                when (next) {
                    TenderQuotaStatus.DRAINING -> return true
                }
                return false
            }
            TenderQuotaStatus.READY -> return next == TenderQuotaStatus.DRAINING || next == TenderQuotaStatus.STOPPED
            TenderQuotaStatus.DRAINING -> {
                when (next) {
                    TenderQuotaStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderQuotaStatus) {
        if (!canMoveTo(next)) {
            error("tender quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = batchSize.toDouble()

    fun snapshot(): TenderQuota {
        return TenderQuota(
            code = code,
            label = label,
            phone = phone,
            reviewer = reviewer,
            ownerId = ownerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tender quota in status idle. */
fun newTenderQuota(code: String, label: String): TenderQuota {
    return TenderQuota(
        code = code,
        label = label,
        phone = "",
        reviewer = "",
        ownerId = "",
        batchSize = 0,
        status = TenderQuotaStatus.IDLE,
    )
}
