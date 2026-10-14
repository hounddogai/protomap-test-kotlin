package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling proof of delivery draft is in its life cycle. */
enum class RollingProofOfDeliveryDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling proof of delivery draft and its current status. */
data class RollingProofOfDeliveryDraft(
    var code: String,
    var name: String,
    var quantity: Int,
    var batchSize: Int,
    var odometer: Int,
    var pallets: Int,
    var updatedAt: Int,
    var category: String,
    var status: RollingProofOfDeliveryDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingProofOfDeliveryDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingProofOfDeliveryDraftStatus): Boolean {
        when (status) {
            RollingProofOfDeliveryDraftStatus.DRAFT -> {
                when (next) {
                    RollingProofOfDeliveryDraftStatus.PENDING, RollingProofOfDeliveryDraftStatus.ACTIVE -> return true
                }
                return false
            }
            RollingProofOfDeliveryDraftStatus.PENDING -> return next == RollingProofOfDeliveryDraftStatus.ACTIVE || next == RollingProofOfDeliveryDraftStatus.SUSPENDED
            RollingProofOfDeliveryDraftStatus.ACTIVE -> {
                when (next) {
                    RollingProofOfDeliveryDraftStatus.SUSPENDED, RollingProofOfDeliveryDraftStatus.CLOSED -> return true
                }
                return false
            }
            RollingProofOfDeliveryDraftStatus.SUSPENDED -> {
                when (next) {
                    RollingProofOfDeliveryDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingProofOfDeliveryDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling proof of delivery draft $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"rolling proof of delivery draft"} $code"

    fun netValue(): Double = round((batchSize.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RollingProofOfDeliveryDraft {
        return RollingProofOfDeliveryDraft(
            code = code,
            name = name,
            quantity = quantity,
            batchSize = batchSize,
            odometer = odometer,
            pallets = pallets,
            updatedAt = updatedAt,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling proof of delivery draft in status draft. */
fun newRollingProofOfDeliveryDraft(code: String, name: String): RollingProofOfDeliveryDraft {
    return RollingProofOfDeliveryDraft(
        code = code,
        name = name,
        quantity = 0,
        batchSize = 0,
        odometer = 0,
        pallets = 0,
        updatedAt = 0,
        category = "",
        status = RollingProofOfDeliveryDraftStatus.DRAFT,
    )
}
