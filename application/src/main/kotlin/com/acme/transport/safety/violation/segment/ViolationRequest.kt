package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a violation request is in its life cycle. */
enum class ViolationRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A violation request as the violation team keeps it. */
data class ViolationRequest(
    var code: String,
    var name: String,
    var discount: Double,
    var attempts: Int,
    var category: String,
    var status: ViolationRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationRequestStatus): Boolean {
        when (status) {
            ViolationRequestStatus.DRAFT -> return next == ViolationRequestStatus.PENDING
            ViolationRequestStatus.PENDING -> {
                when (next) {
                    ViolationRequestStatus.CLOSED -> return true
                }
                return false
            }
            ViolationRequestStatus.ACTIVE -> {
                when (next) {
                    ViolationRequestStatus.SUSPENDED, ViolationRequestStatus.CLOSED -> return true
                }
                return false
            }
            ViolationRequestStatus.SUSPENDED -> {
                when (next) {
                    ViolationRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ViolationRequestStatus) {
        if (!canMoveTo(next)) {
            error("violation request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"violation request"} $code"

    fun projectedLoad(): Double = round((discount - attempts.toDouble()) * 100) / 100

    fun snapshot(): ViolationRequest {
        return ViolationRequest(
            code = code,
            name = name,
            discount = discount,
            attempts = attempts,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a violation request in status draft. */
fun newViolationRequest(code: String, name: String): ViolationRequest {
    return ViolationRequest(
        code = code,
        name = name,
        discount = 0.0,
        attempts = 0,
        category = "",
        status = ViolationRequestStatus.DRAFT,
    )
}
