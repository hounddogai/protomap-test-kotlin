package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered fuel surcharge summary records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a tiered fuel surcharge summary is in its life cycle. */
enum class TieredFuelSurchargeSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One tiered fuel surcharge summary, with what the planners need to act on it. */
data class TieredFuelSurchargeSummary(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var pinned: Boolean,
    var trailerId: String,
    var source: String,
    var stopCount: Int,
    var status: TieredFuelSurchargeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelSurchargeSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelSurchargeSummaryStatus): Boolean {
        when (status) {
            TieredFuelSurchargeSummaryStatus.CREATED -> return next == TieredFuelSurchargeSummaryStatus.VALIDATED || next == TieredFuelSurchargeSummaryStatus.PUBLISHED
            TieredFuelSurchargeSummaryStatus.VALIDATED -> return next == TieredFuelSurchargeSummaryStatus.PUBLISHED || next == TieredFuelSurchargeSummaryStatus.RETIRED
            TieredFuelSurchargeSummaryStatus.PUBLISHED -> {
                when (next) {
                    TieredFuelSurchargeSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelSurchargeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel surcharge summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered fuel surcharge summary"} $code"

    fun adjustedValue(): Double = round((stopCount.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TieredFuelSurchargeSummary {
        return TieredFuelSurchargeSummary(
            code = code,
            title = title,
            expiresAt = expiresAt,
            pinned = pinned,
            trailerId = trailerId,
            source = source,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered fuel surcharge summary in status created. */
fun newTieredFuelSurchargeSummary(code: String, title: String): TieredFuelSurchargeSummary {
    return TieredFuelSurchargeSummary(
        code = code,
        title = title,
        expiresAt = 0,
        pinned = false,
        trailerId = "",
        source = "",
        stopCount = 0,
        status = TieredFuelSurchargeSummaryStatus.CREATED,
    )
}
