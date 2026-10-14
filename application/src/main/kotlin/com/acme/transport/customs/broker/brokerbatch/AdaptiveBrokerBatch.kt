package com.acme.transport.customs.broker.brokerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive broker batch records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a adaptive broker batch is in its life cycle. */
enum class AdaptiveBrokerBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a adaptive broker batch and its current status. */
data class AdaptiveBrokerBatch(
    var code: String,
    var title: String,
    var archived: Boolean,
    var approved: Boolean,
    var source: String,
    var phone: String,
    var shiftCode: String,
    var startsAt: Int,
    var carrier: String,
    var pallets: Int,
    var createdAt: Int,
    var status: AdaptiveBrokerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBrokerBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBrokerBatchStatus): Boolean {
        when (status) {
            AdaptiveBrokerBatchStatus.NEW -> {
                when (next) {
                    AdaptiveBrokerBatchStatus.RUNNING, AdaptiveBrokerBatchStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveBrokerBatchStatus.QUEUED -> {
                when (next) {
                    AdaptiveBrokerBatchStatus.RUNNING, AdaptiveBrokerBatchStatus.DONE -> return true
                }
                return false
            }
            AdaptiveBrokerBatchStatus.RUNNING -> {
                when (next) {
                    AdaptiveBrokerBatchStatus.DONE, AdaptiveBrokerBatchStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveBrokerBatchStatus.DONE -> {
                when (next) {
                    AdaptiveBrokerBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBrokerBatchStatus) {
        if (!canMoveTo(next)) {
            error("adaptive broker batch $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive broker batch"} $code"

    fun netValue(): Double = round(startsAt.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): AdaptiveBrokerBatch {
        return AdaptiveBrokerBatch(
            code = code,
            title = title,
            archived = archived,
            approved = approved,
            source = source,
            phone = phone,
            shiftCode = shiftCode,
            startsAt = startsAt,
            carrier = carrier,
            pallets = pallets,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a adaptive broker batch in status new. */
fun newAdaptiveBrokerBatch(code: String, title: String): AdaptiveBrokerBatch {
    return AdaptiveBrokerBatch(
        code = code,
        title = title,
        archived = false,
        approved = false,
        source = "",
        phone = "",
        shiftCode = "",
        startsAt = 0,
        carrier = "",
        pallets = 0,
        createdAt = 0,
        status = AdaptiveBrokerBatchStatus.NEW,
    )
}
