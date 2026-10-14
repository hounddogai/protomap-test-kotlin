package com.acme.transport.network.zone.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback zone summary is in its life cycle. */
enum class FallbackZoneSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback zone summary, with what the planners need to act on it. */
data class FallbackZoneSummary(
    var code: String,
    var title: String,
    var ceiling: Double,
    var discount: Double,
    var checksum: String,
    var unit: String,
    var pinned: Boolean,
    var minimum: Double,
    var ratio: Double,
    var status: FallbackZoneSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackZoneSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackZoneSummaryStatus): Boolean {
        when (status) {
            FallbackZoneSummaryStatus.PROPOSED -> {
                when (next) {
                    FallbackZoneSummaryStatus.REJECTED -> return true
                }
                return false
            }
            FallbackZoneSummaryStatus.APPROVED -> return next == FallbackZoneSummaryStatus.REJECTED || next == FallbackZoneSummaryStatus.WITHDRAWN
            FallbackZoneSummaryStatus.REJECTED -> {
                when (next) {
                    FallbackZoneSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackZoneSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fallback zone summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback zone summary"} $code"

    fun effectiveValue(): Double = round((ratio + ceiling) * 100) / 100

    fun snapshot(): FallbackZoneSummary {
        return FallbackZoneSummary(
            code = code,
            title = title,
            ceiling = ceiling,
            discount = discount,
            checksum = checksum,
            unit = unit,
            pinned = pinned,
            minimum = minimum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback zone summary in status proposed. */
fun newFallbackZoneSummary(code: String, title: String): FallbackZoneSummary {
    return FallbackZoneSummary(
        code = code,
        title = title,
        ceiling = 0.0,
        discount = 0.0,
        checksum = "",
        unit = "",
        pinned = false,
        minimum = 0.0,
        ratio = 0.0,
        status = FallbackZoneSummaryStatus.PROPOSED,
    )
}
