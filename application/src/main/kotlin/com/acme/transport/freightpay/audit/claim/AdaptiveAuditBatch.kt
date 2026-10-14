package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive audit batch is in its life cycle. */
enum class AdaptiveAuditBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a adaptive audit batch and its current status. */
data class AdaptiveAuditBatch(
    var code: String,
    var name: String,
    var graceDays: Int,
    var description: String,
    var pallets: Int,
    var ratio: Double,
    var channel: String,
    var status: AdaptiveAuditBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAuditBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAuditBatchStatus): Boolean {
        when (status) {
            AdaptiveAuditBatchStatus.DRAFT -> {
                when (next) {
                    AdaptiveAuditBatchStatus.PENDING, AdaptiveAuditBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveAuditBatchStatus.PENDING -> {
                when (next) {
                    AdaptiveAuditBatchStatus.ACTIVE, AdaptiveAuditBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveAuditBatchStatus.ACTIVE -> return next == AdaptiveAuditBatchStatus.SUSPENDED || next == AdaptiveAuditBatchStatus.CLOSED
            AdaptiveAuditBatchStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveAuditBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAuditBatchStatus) {
        if (!canMoveTo(next)) {
            error("adaptive audit batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netTotal(): Double = round(ratio * pallets.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAuditBatch {
        return AdaptiveAuditBatch(
            code = code,
            name = name,
            graceDays = graceDays,
            description = description,
            pallets = pallets,
            ratio = ratio,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive audit batch in status draft. */
fun newAdaptiveAuditBatch(code: String, name: String): AdaptiveAuditBatch {
    return AdaptiveAuditBatch(
        code = code,
        name = name,
        graceDays = 0,
        description = "",
        pallets = 0,
        ratio = 0.0,
        channel = "",
        status = AdaptiveAuditBatchStatus.DRAFT,
    )
}
