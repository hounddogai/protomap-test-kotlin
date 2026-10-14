package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification bundle is in its life cycle. */
enum class PendingQualificationBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One pending qualification bundle, with what the planners need to act on it. */
data class PendingQualificationBundle(
    var code: String,
    var name: String,
    var carrier: String,
    var source: String,
    var capacity: Int,
    var currency: String,
    var shiftCode: String,
    var trailerId: String,
    var minimum: Double,
    var cost: Double,
    var score: Double,
    var status: PendingQualificationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationBundleStatus): Boolean {
        when (status) {
            PendingQualificationBundleStatus.IDLE -> {
                when (next) {
                    PendingQualificationBundleStatus.DRAINING, PendingQualificationBundleStatus.STOPPED -> return true
                }
                return false
            }
            PendingQualificationBundleStatus.WARMING -> return next == PendingQualificationBundleStatus.READY
            PendingQualificationBundleStatus.READY -> return next == PendingQualificationBundleStatus.DRAINING || next == PendingQualificationBundleStatus.STOPPED
            PendingQualificationBundleStatus.DRAINING -> return next == PendingQualificationBundleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PendingQualificationBundleStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((minimum - capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingQualificationBundle {
        return PendingQualificationBundle(
            code = code,
            name = name,
            carrier = carrier,
            source = source,
            capacity = capacity,
            currency = currency,
            shiftCode = shiftCode,
            trailerId = trailerId,
            minimum = minimum,
            cost = cost,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a pending qualification bundle in status idle. */
fun newPendingQualificationBundle(code: String, name: String): PendingQualificationBundle {
    return PendingQualificationBundle(
        code = code,
        name = name,
        carrier = "",
        source = "",
        capacity = 0,
        currency = "",
        shiftCode = "",
        trailerId = "",
        minimum = 0.0,
        cost = 0.0,
        score = 0.0,
        status = PendingQualificationBundleStatus.IDLE,
    )
}
