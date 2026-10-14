package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The cross dock estimate records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a cross dock estimate is in its life cycle. */
enum class CrossDockEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A cross dock estimate as the cross dock team keeps it. */
data class CrossDockEstimate(
    var code: String,
    var title: String,
    var floorValue: Double,
    var revision: Int,
    var shiftCode: String,
    var score: Double,
    var carrier: String,
    var odometer: Int,
    var rate: Double,
    var capacity: Int,
    var batchSize: Int,
    var status: CrossDockEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockEstimateStatus): Boolean {
        when (status) {
            CrossDockEstimateStatus.CREATED -> return next == CrossDockEstimateStatus.VALIDATED || next == CrossDockEstimateStatus.PUBLISHED
            CrossDockEstimateStatus.VALIDATED -> return next == CrossDockEstimateStatus.RETIRED
            CrossDockEstimateStatus.PUBLISHED -> return next == CrossDockEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CrossDockEstimateStatus) {
        if (!canMoveTo(next)) {
            error("cross dock estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"cross dock estimate"} $code"

    fun weightedLoad(): Double = round((rate - batchSize.toDouble()) * 100) / 100

    fun snapshot(): CrossDockEstimate {
        return CrossDockEstimate(
            code = code,
            title = title,
            floorValue = floorValue,
            revision = revision,
            shiftCode = shiftCode,
            score = score,
            carrier = carrier,
            odometer = odometer,
            rate = rate,
            capacity = capacity,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock estimate in status created. */
fun newCrossDockEstimate(code: String, title: String): CrossDockEstimate {
    return CrossDockEstimate(
        code = code,
        title = title,
        floorValue = 0.0,
        revision = 0,
        shiftCode = "",
        score = 0.0,
        carrier = "",
        odometer = 0,
        rate = 0.0,
        capacity = 0,
        batchSize = 0,
        status = CrossDockEstimateStatus.CREATED,
    )
}
