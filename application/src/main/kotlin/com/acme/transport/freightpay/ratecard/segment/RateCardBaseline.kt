package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card baseline is in its life cycle. */
enum class RateCardBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rate card baseline. Its code is unique within a tenant. */
data class RateCardBaseline(
    var code: String,
    var label: String,
    var ownerId: String,
    var ratio: Double,
    var margin: Double,
    var createdAt: Int,
    var checksum: String,
    var status: RateCardBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardBaselineStatus): Boolean {
        when (status) {
            RateCardBaselineStatus.NEW -> return next == RateCardBaselineStatus.RUNNING || next == RateCardBaselineStatus.DONE
            RateCardBaselineStatus.QUEUED -> return next == RateCardBaselineStatus.RUNNING || next == RateCardBaselineStatus.DONE
            RateCardBaselineStatus.RUNNING -> {
                when (next) {
                    RateCardBaselineStatus.DONE, RateCardBaselineStatus.FAILED -> return true
                }
                return false
            }
            RateCardBaselineStatus.DONE -> return next == RateCardBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RateCardBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rate card baseline $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedVolume(): Double = round((ratio - margin) * 100) / 100

    fun snapshot(): RateCardBaseline {
        return RateCardBaseline(
            code = code,
            label = label,
            ownerId = ownerId,
            ratio = ratio,
            margin = margin,
            createdAt = createdAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rate card baseline in status new. */
fun newRateCardBaseline(code: String, label: String): RateCardBaseline {
    return RateCardBaseline(
        code = code,
        label = label,
        ownerId = "",
        ratio = 0.0,
        margin = 0.0,
        createdAt = 0,
        checksum = "",
        status = RateCardBaselineStatus.NEW,
    )
}
