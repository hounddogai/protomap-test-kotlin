package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dispute record is in its life cycle. */
enum class WeightedDisputeRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted dispute record as the dispute team keeps it. */
data class WeightedDisputeRecord(
    var code: String,
    var label: String,
    var active: Boolean,
    var currency: String,
    var description: String,
    var priority: Int,
    var expiresAt: Int,
    var retries: Int,
    var batchSize: Int,
    var status: WeightedDisputeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDisputeRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDisputeRecordStatus): Boolean {
        when (status) {
            WeightedDisputeRecordStatus.OPEN -> return next == WeightedDisputeRecordStatus.IN_REVIEW
            WeightedDisputeRecordStatus.IN_REVIEW -> {
                when (next) {
                    WeightedDisputeRecordStatus.RESOLVED, WeightedDisputeRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedDisputeRecordStatus.RESOLVED -> {
                when (next) {
                    WeightedDisputeRecordStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedDisputeRecordStatus.REOPENED -> {
                when (next) {
                    WeightedDisputeRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedDisputeRecordStatus) {
        if (!canMoveTo(next)) {
            error("weighted dispute record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedLoad(): Double = round(priority.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): WeightedDisputeRecord {
        return WeightedDisputeRecord(
            code = code,
            label = label,
            active = active,
            currency = currency,
            description = description,
            priority = priority,
            expiresAt = expiresAt,
            retries = retries,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted dispute record in status open. */
fun newWeightedDisputeRecord(code: String, label: String): WeightedDisputeRecord {
    return WeightedDisputeRecord(
        code = code,
        label = label,
        active = false,
        currency = "",
        description = "",
        priority = 0,
        expiresAt = 0,
        retries = 0,
        batchSize = 0,
        status = WeightedDisputeRecordStatus.OPEN,
    )
}
