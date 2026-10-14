package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending cross dock variance is in its life cycle. */
enum class PendingCrossDockVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending cross dock variance, with what the planners need to act on it. */
data class PendingCrossDockVariance(
    var code: String,
    var name: String,
    var archived: Boolean,
    var discount: Double,
    var createdAt: Int,
    var locale: String,
    var depot: String,
    var category: String,
    var amount: Double,
    var status: PendingCrossDockVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingCrossDockVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingCrossDockVarianceStatus): Boolean {
        when (status) {
            PendingCrossDockVarianceStatus.PLANNED -> return next == PendingCrossDockVarianceStatus.COMPLETED || next == PendingCrossDockVarianceStatus.CANCELLED
            PendingCrossDockVarianceStatus.SCHEDULED -> {
                when (next) {
                    PendingCrossDockVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            PendingCrossDockVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    PendingCrossDockVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            PendingCrossDockVarianceStatus.COMPLETED -> {
                when (next) {
                    PendingCrossDockVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingCrossDockVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending cross dock variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((amount + discount) * 100) / 100

    fun snapshot(): PendingCrossDockVariance {
        return PendingCrossDockVariance(
            code = code,
            name = name,
            archived = archived,
            discount = discount,
            createdAt = createdAt,
            locale = locale,
            depot = depot,
            category = category,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending cross dock variance in status planned. */
fun newPendingCrossDockVariance(code: String, name: String): PendingCrossDockVariance {
    return PendingCrossDockVariance(
        code = code,
        name = name,
        archived = false,
        discount = 0.0,
        createdAt = 0,
        locale = "",
        depot = "",
        category = "",
        amount = 0.0,
        status = PendingCrossDockVarianceStatus.PLANNED,
    )
}
