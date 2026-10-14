package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled emission factor batch is in its life cycle. */
enum class ScheduledEmissionFactorBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled emission factor batch, with what the planners need to act on it. */
data class ScheduledEmissionFactorBatch(
    var code: String,
    var name: String,
    var threshold: Double,
    var locale: String,
    var version: Int,
    var etaMinutes: Int,
    var stopCount: Int,
    var expiresAt: Int,
    var contactEmail: String,
    var lane: String,
    var status: ScheduledEmissionFactorBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledEmissionFactorBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledEmissionFactorBatchStatus): Boolean {
        when (status) {
            ScheduledEmissionFactorBatchStatus.NEW -> {
                when (next) {
                    ScheduledEmissionFactorBatchStatus.QUEUED, ScheduledEmissionFactorBatchStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledEmissionFactorBatchStatus.QUEUED -> return next == ScheduledEmissionFactorBatchStatus.DONE || next == ScheduledEmissionFactorBatchStatus.FAILED
            ScheduledEmissionFactorBatchStatus.RUNNING -> {
                when (next) {
                    ScheduledEmissionFactorBatchStatus.DONE -> return true
                }
                return false
            }
            ScheduledEmissionFactorBatchStatus.DONE -> {
                when (next) {
                    ScheduledEmissionFactorBatchStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledEmissionFactorBatchStatus) {
        if (!canMoveTo(next)) {
            error("scheduled emission factor batch $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"scheduled emission factor batch"} $code"

    fun effectiveValue(): Double = round(stopCount.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ScheduledEmissionFactorBatch {
        return ScheduledEmissionFactorBatch(
            code = code,
            name = name,
            threshold = threshold,
            locale = locale,
            version = version,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            expiresAt = expiresAt,
            contactEmail = contactEmail,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled emission factor batch in status new. */
fun newScheduledEmissionFactorBatch(code: String, name: String): ScheduledEmissionFactorBatch {
    return ScheduledEmissionFactorBatch(
        code = code,
        name = name,
        threshold = 0.0,
        locale = "",
        version = 0,
        etaMinutes = 0,
        stopCount = 0,
        expiresAt = 0,
        contactEmail = "",
        lane = "",
        status = ScheduledEmissionFactorBatchStatus.NEW,
    )
}
