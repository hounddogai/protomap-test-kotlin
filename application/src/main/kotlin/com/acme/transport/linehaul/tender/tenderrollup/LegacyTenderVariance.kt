package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy tender variance is in its life cycle. */
enum class LegacyTenderVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy tender variance as the tender team keeps it. */
data class LegacyTenderVariance(
    var code: String,
    var label: String,
    var attempts: Int,
    var createdAt: Int,
    var quantity: Int,
    var status: LegacyTenderVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTenderVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTenderVarianceStatus): Boolean {
        when (status) {
            LegacyTenderVarianceStatus.DRAFT -> return next == LegacyTenderVarianceStatus.ACTIVE
            LegacyTenderVarianceStatus.PENDING -> {
                when (next) {
                    LegacyTenderVarianceStatus.CLOSED -> return true
                }
                return false
            }
            LegacyTenderVarianceStatus.ACTIVE -> return next == LegacyTenderVarianceStatus.SUSPENDED || next == LegacyTenderVarianceStatus.CLOSED
            LegacyTenderVarianceStatus.SUSPENDED -> {
                when (next) {
                    LegacyTenderVarianceStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTenderVarianceStatus) {
        if (!canMoveTo(next)) {
            error("legacy tender variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((createdAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): LegacyTenderVariance {
        return LegacyTenderVariance(
            code = code,
            label = label,
            attempts = attempts,
            createdAt = createdAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy tender variance in status draft. */
fun newLegacyTenderVariance(code: String, label: String): LegacyTenderVariance {
    return LegacyTenderVariance(
        code = code,
        label = label,
        attempts = 0,
        createdAt = 0,
        quantity = 0,
        status = LegacyTenderVarianceStatus.DRAFT,
    )
}
