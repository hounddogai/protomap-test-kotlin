package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery claim is in its life cycle. */
enum class RedeliveryClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A redelivery claim. Its code is unique within a tenant. */
data class RedeliveryClaim(
    var code: String,
    var label: String,
    var lane: String,
    var batchSize: Int,
    var trailerId: String,
    var region: String,
    var title: String,
    var rate: Double,
    var reviewer: String,
    var loadKg: Double,
    var status: RedeliveryClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryClaimStatus): Boolean {
        when (status) {
            RedeliveryClaimStatus.NEW -> return next == RedeliveryClaimStatus.DONE || next == RedeliveryClaimStatus.FAILED
            RedeliveryClaimStatus.QUEUED -> {
                when (next) {
                    RedeliveryClaimStatus.DONE, RedeliveryClaimStatus.FAILED -> return true
                }
                return false
            }
            RedeliveryClaimStatus.RUNNING -> return next == RedeliveryClaimStatus.DONE || next == RedeliveryClaimStatus.FAILED
            RedeliveryClaimStatus.DONE -> return next == RedeliveryClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RedeliveryClaimStatus) {
        if (!canMoveTo(next)) {
            error("redelivery claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((rate + batchSize.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryClaim {
        return RedeliveryClaim(
            code = code,
            label = label,
            lane = lane,
            batchSize = batchSize,
            trailerId = trailerId,
            region = region,
            title = title,
            rate = rate,
            reviewer = reviewer,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a redelivery claim in status new. */
fun newRedeliveryClaim(code: String, label: String): RedeliveryClaim {
    return RedeliveryClaim(
        code = code,
        label = label,
        lane = "",
        batchSize = 0,
        trailerId = "",
        region = "",
        title = "",
        rate = 0.0,
        reviewer = "",
        loadKg = 0.0,
        status = RedeliveryClaimStatus.NEW,
    )
}
