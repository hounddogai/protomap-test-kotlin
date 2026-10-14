package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped transit time summary is in its life cycle. */
enum class CappedTransitTimeSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A capped transit time summary. Its code is unique within a tenant. */
data class CappedTransitTimeSummary(
    var code: String,
    var name: String,
    var lane: String,
    var externalRef: String,
    var ratio: Double,
    var endsAt: Int,
    var status: CappedTransitTimeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTransitTimeSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTransitTimeSummaryStatus): Boolean {
        when (status) {
            CappedTransitTimeSummaryStatus.DRAFT -> {
                when (next) {
                    CappedTransitTimeSummaryStatus.PENDING, CappedTransitTimeSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedTransitTimeSummaryStatus.PENDING -> {
                when (next) {
                    CappedTransitTimeSummaryStatus.ACTIVE, CappedTransitTimeSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedTransitTimeSummaryStatus.ACTIVE -> {
                when (next) {
                    CappedTransitTimeSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedTransitTimeSummaryStatus.SUSPENDED -> return next == CappedTransitTimeSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: CappedTransitTimeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("capped transit time summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((endsAt.toDouble() - ratio) * 100) / 100

    fun snapshot(): CappedTransitTimeSummary {
        return CappedTransitTimeSummary(
            code = code,
            name = name,
            lane = lane,
            externalRef = externalRef,
            ratio = ratio,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped transit time summary in status draft. */
fun newCappedTransitTimeSummary(code: String, name: String): CappedTransitTimeSummary {
    return CappedTransitTimeSummary(
        code = code,
        name = name,
        lane = "",
        externalRef = "",
        ratio = 0.0,
        endsAt = 0,
        status = CappedTransitTimeSummaryStatus.DRAFT,
    )
}
