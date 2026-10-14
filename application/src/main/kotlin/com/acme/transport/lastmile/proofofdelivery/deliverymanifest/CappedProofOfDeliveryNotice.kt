package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped proof of delivery notice is in its life cycle. */
enum class CappedProofOfDeliveryNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capped proof of delivery notice and its current status. */
data class CappedProofOfDeliveryNotice(
    var code: String,
    var name: String,
    var version: Int,
    var discount: Double,
    var stopCount: Int,
    var priority: Int,
    var revision: Int,
    var batchSize: Int,
    var etaMinutes: Int,
    var visible: Boolean,
    var amount: Double,
    var status: CappedProofOfDeliveryNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedProofOfDeliveryNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedProofOfDeliveryNoticeStatus): Boolean {
        when (status) {
            CappedProofOfDeliveryNoticeStatus.PLANNED -> return next == CappedProofOfDeliveryNoticeStatus.SCHEDULED || next == CappedProofOfDeliveryNoticeStatus.CANCELLED
            CappedProofOfDeliveryNoticeStatus.SCHEDULED -> return next == CappedProofOfDeliveryNoticeStatus.CANCELLED
            CappedProofOfDeliveryNoticeStatus.IN_PROGRESS -> return next == CappedProofOfDeliveryNoticeStatus.CANCELLED
            CappedProofOfDeliveryNoticeStatus.COMPLETED -> {
                when (next) {
                    CappedProofOfDeliveryNoticeStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedProofOfDeliveryNoticeStatus) {
        if (!canMoveTo(next)) {
            error("capped proof of delivery notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"capped proof of delivery notice"} $code"

    fun netTotal(): Double = round((etaMinutes.toDouble() + discount) * 100) / 100

    fun snapshot(): CappedProofOfDeliveryNotice {
        return CappedProofOfDeliveryNotice(
            code = code,
            name = name,
            version = version,
            discount = discount,
            stopCount = stopCount,
            priority = priority,
            revision = revision,
            batchSize = batchSize,
            etaMinutes = etaMinutes,
            visible = visible,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped proof of delivery notice in status planned. */
fun newCappedProofOfDeliveryNotice(code: String, name: String): CappedProofOfDeliveryNotice {
    return CappedProofOfDeliveryNotice(
        code = code,
        name = name,
        version = 0,
        discount = 0.0,
        stopCount = 0,
        priority = 0,
        revision = 0,
        batchSize = 0,
        etaMinutes = 0,
        visible = false,
        amount = 0.0,
        status = CappedProofOfDeliveryNoticeStatus.PLANNED,
    )
}
