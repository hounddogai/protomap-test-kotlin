package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation exception records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation exception is in its life cycle. */
enum class ViolationExceptionStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A violation exception. Its code is unique within a tenant. */
data class ViolationException(
    var code: String,
    var label: String,
    var rate: Double,
    var batchSize: Int,
    var loadKg: Double,
    var description: String,
    var cost: Double,
    var carrier: String,
    var limit: Int,
    var status: ViolationExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationExceptionStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationExceptionStatus): Boolean {
        when (status) {
            ViolationExceptionStatus.DRAFT -> {
                when (next) {
                    ViolationExceptionStatus.SUSPENDED, ViolationExceptionStatus.CLOSED -> return true
                }
                return false
            }
            ViolationExceptionStatus.PENDING -> return next == ViolationExceptionStatus.SUSPENDED || next == ViolationExceptionStatus.CLOSED
            ViolationExceptionStatus.ACTIVE -> {
                when (next) {
                    ViolationExceptionStatus.SUSPENDED, ViolationExceptionStatus.CLOSED -> return true
                }
                return false
            }
            ViolationExceptionStatus.SUSPENDED -> {
                when (next) {
                    ViolationExceptionStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationExceptionStatus) {
        if (!canMoveTo(next)) {
            error("violation exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"violation exception"} $code"

    fun weightedLoad(): Double = round((batchSize.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): ViolationException {
        return ViolationException(
            code = code,
            label = label,
            rate = rate,
            batchSize = batchSize,
            loadKg = loadKg,
            description = description,
            cost = cost,
            carrier = carrier,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a violation exception in status draft. */
fun newViolationException(code: String, label: String): ViolationException {
    return ViolationException(
        code = code,
        label = label,
        rate = 0.0,
        batchSize = 0,
        loadKg = 0.0,
        description = "",
        cost = 0.0,
        carrier = "",
        limit = 0,
        status = ViolationExceptionStatus.DRAFT,
    )
}
