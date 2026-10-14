package com.acme.transport.network.hub.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped hub baseline is in its life cycle. */
enum class CappedHubBaselineStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped hub baseline. Its code is unique within a tenant. */
data class CappedHubBaseline(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var retries: Int,
    var trailerId: String,
    var batchSize: Int,
    var externalRef: String,
    var startsAt: Int,
    var source: String,
    var margin: Double,
    var status: CappedHubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedHubBaselineStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedHubBaselineStatus): Boolean {
        when (status) {
            CappedHubBaselineStatus.NEW -> {
                when (next) {
                    CappedHubBaselineStatus.FAILED -> return true
                }
                return false
            }
            CappedHubBaselineStatus.QUEUED -> return next == CappedHubBaselineStatus.RUNNING
            CappedHubBaselineStatus.RUNNING -> return next == CappedHubBaselineStatus.DONE || next == CappedHubBaselineStatus.FAILED
            CappedHubBaselineStatus.DONE -> return next == CappedHubBaselineStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedHubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("capped hub baseline $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped hub baseline"} $code"

    fun projectedExposure(): Double = round((batchSize.toDouble() + margin) * 100) / 100

    fun snapshot(): CappedHubBaseline {
        return CappedHubBaseline(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            retries = retries,
            trailerId = trailerId,
            batchSize = batchSize,
            externalRef = externalRef,
            startsAt = startsAt,
            source = source,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped hub baseline in status new. */
fun newCappedHubBaseline(code: String, label: String): CappedHubBaseline {
    return CappedHubBaseline(
        code = code,
        label = label,
        durationMinutes = 0,
        retries = 0,
        trailerId = "",
        batchSize = 0,
        externalRef = "",
        startsAt = 0,
        source = "",
        margin = 0.0,
        status = CappedHubBaselineStatus.NEW,
    )
}
