package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary clearance variance is in its life cycle. */
enum class PrimaryClearanceVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A primary clearance variance. Its code is unique within a tenant. */
data class PrimaryClearanceVariance(
    var code: String,
    var title: String,
    var shiftCode: String,
    var expiresAt: Int,
    var fuelLiters: Double,
    var summary: String,
    var segment: String,
    var rate: Double,
    var limit: Int,
    var status: PrimaryClearanceVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryClearanceVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryClearanceVarianceStatus): Boolean {
        when (status) {
            PrimaryClearanceVarianceStatus.NEW -> return next == PrimaryClearanceVarianceStatus.QUEUED || next == PrimaryClearanceVarianceStatus.RUNNING
            PrimaryClearanceVarianceStatus.QUEUED -> return next == PrimaryClearanceVarianceStatus.RUNNING || next == PrimaryClearanceVarianceStatus.DONE
            PrimaryClearanceVarianceStatus.RUNNING -> {
                when (next) {
                    PrimaryClearanceVarianceStatus.FAILED -> return true
                }
                return false
            }
            PrimaryClearanceVarianceStatus.DONE -> {
                when (next) {
                    PrimaryClearanceVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryClearanceVarianceStatus) {
        if (!canMoveTo(next)) {
            error("primary clearance variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round((expiresAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): PrimaryClearanceVariance {
        return PrimaryClearanceVariance(
            code = code,
            title = title,
            shiftCode = shiftCode,
            expiresAt = expiresAt,
            fuelLiters = fuelLiters,
            summary = summary,
            segment = segment,
            rate = rate,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary clearance variance in status new. */
fun newPrimaryClearanceVariance(code: String, title: String): PrimaryClearanceVariance {
    return PrimaryClearanceVariance(
        code = code,
        title = title,
        shiftCode = "",
        expiresAt = 0,
        fuelLiters = 0.0,
        summary = "",
        segment = "",
        rate = 0.0,
        limit = 0,
        status = PrimaryClearanceVarianceStatus.NEW,
    )
}
