package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped swap agreement is in its life cycle. */
enum class CappedSwapAgreementStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped swap agreement and its current status. */
data class CappedSwapAgreement(
    var code: String,
    var name: String,
    var retries: Int,
    var trailerId: String,
    var country: String,
    var endsAt: Int,
    var ratio: Double,
    var category: String,
    var limit: Int,
    var windowDays: Int,
    var status: CappedSwapAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSwapAgreementStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSwapAgreementStatus): Boolean {
        when (status) {
            CappedSwapAgreementStatus.IDLE -> {
                when (next) {
                    CappedSwapAgreementStatus.READY -> return true
                }
                return false
            }
            CappedSwapAgreementStatus.WARMING -> return next == CappedSwapAgreementStatus.DRAINING
            CappedSwapAgreementStatus.READY -> {
                when (next) {
                    CappedSwapAgreementStatus.DRAINING -> return true
                }
                return false
            }
            CappedSwapAgreementStatus.DRAINING -> return next == CappedSwapAgreementStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedSwapAgreementStatus) {
        if (!canMoveTo(next)) {
            error("capped swap agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((endsAt.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): CappedSwapAgreement {
        return CappedSwapAgreement(
            code = code,
            name = name,
            retries = retries,
            trailerId = trailerId,
            country = country,
            endsAt = endsAt,
            ratio = ratio,
            category = category,
            limit = limit,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped swap agreement in status idle. */
fun newCappedSwapAgreement(code: String, name: String): CappedSwapAgreement {
    return CappedSwapAgreement(
        code = code,
        name = name,
        retries = 0,
        trailerId = "",
        country = "",
        endsAt = 0,
        ratio = 0.0,
        category = "",
        limit = 0,
        windowDays = 0,
        status = CappedSwapAgreementStatus.IDLE,
    )
}
