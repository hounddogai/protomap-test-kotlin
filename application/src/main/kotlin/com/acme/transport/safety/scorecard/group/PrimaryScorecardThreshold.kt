package com.acme.transport.safety.scorecard.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary scorecard threshold is in its life cycle. */
enum class PrimaryScorecardThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a primary scorecard threshold and its current status. */
data class PrimaryScorecardThreshold(
    var code: String,
    var title: String,
    var currency: String,
    var revision: Int,
    var score: Double,
    var externalRef: String,
    var status: PrimaryScorecardThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardThresholdStatus): Boolean {
        when (status) {
            PrimaryScorecardThresholdStatus.NEW -> return next == PrimaryScorecardThresholdStatus.RUNNING
            PrimaryScorecardThresholdStatus.QUEUED -> {
                when (next) {
                    PrimaryScorecardThresholdStatus.DONE, PrimaryScorecardThresholdStatus.FAILED -> return true
                }
                return false
            }
            PrimaryScorecardThresholdStatus.RUNNING -> return next == PrimaryScorecardThresholdStatus.DONE
            PrimaryScorecardThresholdStatus.DONE -> {
                when (next) {
                    PrimaryScorecardThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardThresholdStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard threshold $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round((score + revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryScorecardThreshold {
        return PrimaryScorecardThreshold(
            code = code,
            title = title,
            currency = currency,
            revision = revision,
            score = score,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary scorecard threshold in status new. */
fun newPrimaryScorecardThreshold(code: String, title: String): PrimaryScorecardThreshold {
    return PrimaryScorecardThreshold(
        code = code,
        title = title,
        currency = "",
        revision = 0,
        score = 0.0,
        externalRef = "",
        status = PrimaryScorecardThresholdStatus.NEW,
    )
}
