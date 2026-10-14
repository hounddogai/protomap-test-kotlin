package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched violation tier is in its life cycle. */
enum class BatchedViolationTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched violation tier. Its code is unique within a tenant. */
data class BatchedViolationTier(
    var code: String,
    var label: String,
    var loadKg: Double,
    var driverId: String,
    var expiresAt: Int,
    var category: String,
    var stopCount: Int,
    var graceDays: Int,
    var phone: String,
    var status: BatchedViolationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedViolationTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedViolationTierStatus): Boolean {
        when (status) {
            BatchedViolationTierStatus.PROPOSED -> {
                when (next) {
                    BatchedViolationTierStatus.APPROVED -> return true
                }
                return false
            }
            BatchedViolationTierStatus.APPROVED -> {
                when (next) {
                    BatchedViolationTierStatus.REJECTED -> return true
                }
                return false
            }
            BatchedViolationTierStatus.REJECTED -> {
                when (next) {
                    BatchedViolationTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedViolationTierStatus) {
        if (!canMoveTo(next)) {
            error("batched violation tier $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched violation tier"} $code"

    fun weightedTotal(): Double = round((graceDays.toDouble() + loadKg) * 100) / 100

    fun snapshot(): BatchedViolationTier {
        return BatchedViolationTier(
            code = code,
            label = label,
            loadKg = loadKg,
            driverId = driverId,
            expiresAt = expiresAt,
            category = category,
            stopCount = stopCount,
            graceDays = graceDays,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched violation tier in status proposed. */
fun newBatchedViolationTier(code: String, label: String): BatchedViolationTier {
    return BatchedViolationTier(
        code = code,
        label = label,
        loadKg = 0.0,
        driverId = "",
        expiresAt = 0,
        category = "",
        stopCount = 0,
        graceDays = 0,
        phone = "",
        status = BatchedViolationTierStatus.PROPOSED,
    )
}
