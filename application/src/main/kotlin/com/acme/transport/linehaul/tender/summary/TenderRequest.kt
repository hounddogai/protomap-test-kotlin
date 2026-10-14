package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender request is in its life cycle. */
enum class TenderRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tender request. Its code is unique within a tenant. */
data class TenderRequest(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var retries: Int,
    var lane: String,
    var status: TenderRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderRequestStatus): Boolean {
        when (status) {
            TenderRequestStatus.REQUESTED -> return next == TenderRequestStatus.GRANTED || next == TenderRequestStatus.EXPIRED
            TenderRequestStatus.GRANTED -> {
                when (next) {
                    TenderRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TenderRequestStatus.REVOKED -> {
                when (next) {
                    TenderRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderRequestStatus) {
        if (!canMoveTo(next)) {
            error("tender request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tender request"} $code"

    fun adjustedValue(): Double = round((retries.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TenderRequest {
        return TenderRequest(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            retries = retries,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tender request in status requested. */
fun newTenderRequest(code: String, title: String): TenderRequest {
    return TenderRequest(
        code = code,
        title = title,
        etaMinutes = 0,
        retries = 0,
        lane = "",
        status = TenderRequestStatus.REQUESTED,
    )
}
