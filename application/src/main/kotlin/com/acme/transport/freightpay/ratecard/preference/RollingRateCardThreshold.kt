package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling rate card threshold is in its life cycle. */
enum class RollingRateCardThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling rate card threshold, with what the planners need to act on it. */
data class RollingRateCardThreshold(
    var code: String,
    var label: String,
    var threshold: Double,
    var batchSize: Int,
    var version: Int,
    var status: RollingRateCardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRateCardThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRateCardThresholdStatus): Boolean {
        when (status) {
            RollingRateCardThresholdStatus.NEW -> return next == RollingRateCardThresholdStatus.RUNNING
            RollingRateCardThresholdStatus.QUEUED -> {
                when (next) {
                    RollingRateCardThresholdStatus.RUNNING, RollingRateCardThresholdStatus.FAILED -> return true
                }
                return false
            }
            RollingRateCardThresholdStatus.RUNNING -> {
                when (next) {
                    RollingRateCardThresholdStatus.DONE, RollingRateCardThresholdStatus.FAILED -> return true
                }
                return false
            }
            RollingRateCardThresholdStatus.DONE -> return next == RollingRateCardThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingRateCardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("rolling rate card threshold $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"rolling rate card threshold"} $code"

    fun netTotal(): Double = round((threshold - version.toDouble()) * 100) / 100

    fun snapshot(): RollingRateCardThreshold {
        return RollingRateCardThreshold(
            code = code,
            label = label,
            threshold = threshold,
            batchSize = batchSize,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling rate card threshold in status new. */
fun newRollingRateCardThreshold(code: String, label: String): RollingRateCardThreshold {
    return RollingRateCardThreshold(
        code = code,
        label = label,
        threshold = 0.0,
        batchSize = 0,
        version = 0,
        status = RollingRateCardThresholdStatus.NEW,
    )
}
