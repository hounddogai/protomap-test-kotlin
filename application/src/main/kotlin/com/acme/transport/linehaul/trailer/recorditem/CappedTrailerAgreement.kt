package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped trailer agreement is in its life cycle. */
enum class CappedTrailerAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped trailer agreement as the trailer team keeps it. */
data class CappedTrailerAgreement(
    var code: String,
    var title: String,
    var floorValue: Double,
    var segment: String,
    var approved: Boolean,
    var limit: Int,
    var vehicleId: String,
    var status: CappedTrailerAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerAgreementStatus): Boolean {
        when (status) {
            CappedTrailerAgreementStatus.IDLE -> return next == CappedTrailerAgreementStatus.STOPPED
            CappedTrailerAgreementStatus.WARMING -> return next == CappedTrailerAgreementStatus.DRAINING
            CappedTrailerAgreementStatus.READY -> {
                when (next) {
                    CappedTrailerAgreementStatus.DRAINING, CappedTrailerAgreementStatus.STOPPED -> return true
                }
                return false
            }
            CappedTrailerAgreementStatus.DRAINING -> {
                when (next) {
                    CappedTrailerAgreementStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedExposure(): Double = round((limit.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CappedTrailerAgreement {
        return CappedTrailerAgreement(
            code = code,
            title = title,
            floorValue = floorValue,
            segment = segment,
            approved = approved,
            limit = limit,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped trailer agreement in status idle. */
fun newCappedTrailerAgreement(code: String, title: String): CappedTrailerAgreement {
    return CappedTrailerAgreement(
        code = code,
        title = title,
        floorValue = 0.0,
        segment = "",
        approved = false,
        limit = 0,
        vehicleId = "",
        status = CappedTrailerAgreementStatus.IDLE,
    )
}
