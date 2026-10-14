package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient dock door review records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a lenient dock door review is in its life cycle. */
enum class LenientDockDoorReviewStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient dock door review and its current status. */
data class LenientDockDoorReview(
    var code: String,
    var name: String,
    var quantity: Int,
    var capacity: Int,
    var approved: Boolean,
    var vehicleId: String,
    var loadKg: Double,
    var shiftCode: String,
    var limit: Int,
    var status: LenientDockDoorReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDockDoorReviewStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDockDoorReviewStatus): Boolean {
        when (status) {
            LenientDockDoorReviewStatus.NEW -> {
                when (next) {
                    LenientDockDoorReviewStatus.QUEUED -> return true
                }
                return false
            }
            LenientDockDoorReviewStatus.QUEUED -> return next == LenientDockDoorReviewStatus.DONE
            LenientDockDoorReviewStatus.RUNNING -> return next == LenientDockDoorReviewStatus.DONE || next == LenientDockDoorReviewStatus.FAILED
            LenientDockDoorReviewStatus.DONE -> return next == LenientDockDoorReviewStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientDockDoorReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient dock door review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((loadKg + capacity.toDouble()) * 100) / 100

    fun snapshot(): LenientDockDoorReview {
        return LenientDockDoorReview(
            code = code,
            name = name,
            quantity = quantity,
            capacity = capacity,
            approved = approved,
            vehicleId = vehicleId,
            loadKg = loadKg,
            shiftCode = shiftCode,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient dock door review in status new. */
fun newLenientDockDoorReview(code: String, name: String): LenientDockDoorReview {
    return LenientDockDoorReview(
        code = code,
        name = name,
        quantity = 0,
        capacity = 0,
        approved = false,
        vehicleId = "",
        loadKg = 0.0,
        shiftCode = "",
        limit = 0,
        status = LenientDockDoorReviewStatus.NEW,
    )
}
