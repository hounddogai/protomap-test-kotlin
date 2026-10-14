package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient relay summary is in its life cycle. */
enum class LenientRelaySummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A lenient relay summary as the relay team keeps it. */
data class LenientRelaySummary(
    var code: String,
    var title: String,
    var phone: String,
    var discount: Double,
    var weight: Double,
    var graceDays: Int,
    var trailerId: String,
    var active: Boolean,
    var status: LenientRelaySummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRelaySummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRelaySummaryStatus): Boolean {
        when (status) {
            LenientRelaySummaryStatus.CREATED -> return next == LenientRelaySummaryStatus.VALIDATED || next == LenientRelaySummaryStatus.PUBLISHED
            LenientRelaySummaryStatus.VALIDATED -> return next == LenientRelaySummaryStatus.PUBLISHED || next == LenientRelaySummaryStatus.RETIRED
            LenientRelaySummaryStatus.PUBLISHED -> return next == LenientRelaySummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRelaySummaryStatus) {
        if (!canMoveTo(next)) {
            error("lenient relay summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((weight - discount) * 100) / 100

    fun snapshot(): LenientRelaySummary {
        return LenientRelaySummary(
            code = code,
            title = title,
            phone = phone,
            discount = discount,
            weight = weight,
            graceDays = graceDays,
            trailerId = trailerId,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient relay summary in status created. */
fun newLenientRelaySummary(code: String, title: String): LenientRelaySummary {
    return LenientRelaySummary(
        code = code,
        title = title,
        phone = "",
        discount = 0.0,
        weight = 0.0,
        graceDays = 0,
        trailerId = "",
        active = false,
        status = LenientRelaySummaryStatus.CREATED,
    )
}
