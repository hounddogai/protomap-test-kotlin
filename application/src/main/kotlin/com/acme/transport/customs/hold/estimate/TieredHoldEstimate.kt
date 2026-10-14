package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered hold estimate records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a tiered hold estimate is in its life cycle. */
enum class TieredHoldEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered hold estimate. Its code is unique within a tenant. */
data class TieredHoldEstimate(
    var code: String,
    var name: String,
    var priority: Int,
    var active: Boolean,
    var sequence: Int,
    var limit: Int,
    var threshold: Double,
    var externalRef: String,
    var routeCode: String,
    var visible: Boolean,
    var carrier: String,
    var status: TieredHoldEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredHoldEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredHoldEstimateStatus): Boolean {
        when (status) {
            TieredHoldEstimateStatus.NEW -> return next == TieredHoldEstimateStatus.DONE || next == TieredHoldEstimateStatus.FAILED
            TieredHoldEstimateStatus.QUEUED -> return next == TieredHoldEstimateStatus.RUNNING
            TieredHoldEstimateStatus.RUNNING -> return next == TieredHoldEstimateStatus.DONE
            TieredHoldEstimateStatus.DONE -> return next == TieredHoldEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredHoldEstimateStatus) {
        if (!canMoveTo(next)) {
            error("tiered hold estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((threshold + sequence.toDouble()) * 100) / 100

    fun snapshot(): TieredHoldEstimate {
        return TieredHoldEstimate(
            code = code,
            name = name,
            priority = priority,
            active = active,
            sequence = sequence,
            limit = limit,
            threshold = threshold,
            externalRef = externalRef,
            routeCode = routeCode,
            visible = visible,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered hold estimate in status new. */
fun newTieredHoldEstimate(code: String, name: String): TieredHoldEstimate {
    return TieredHoldEstimate(
        code = code,
        name = name,
        priority = 0,
        active = false,
        sequence = 0,
        limit = 0,
        threshold = 0.0,
        externalRef = "",
        routeCode = "",
        visible = false,
        carrier = "",
        status = TieredHoldEstimateStatus.NEW,
    )
}
