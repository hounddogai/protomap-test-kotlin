package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered parcel summary is in its life cycle. */
enum class TieredParcelSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered parcel summary and its current status. */
data class TieredParcelSummary(
    var code: String,
    var name: String,
    var windowDays: Int,
    var routeCode: String,
    var updatedAt: Int,
    var status: TieredParcelSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredParcelSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredParcelSummaryStatus): Boolean {
        when (status) {
            TieredParcelSummaryStatus.REQUESTED -> {
                when (next) {
                    TieredParcelSummaryStatus.REVOKED -> return true
                }
                return false
            }
            TieredParcelSummaryStatus.GRANTED -> return next == TieredParcelSummaryStatus.REVOKED
            TieredParcelSummaryStatus.REVOKED -> {
                when (next) {
                    TieredParcelSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredParcelSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tiered parcel summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered parcel summary"} $code"

    fun netExposure(): Double = round((updatedAt.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): TieredParcelSummary {
        return TieredParcelSummary(
            code = code,
            name = name,
            windowDays = windowDays,
            routeCode = routeCode,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered parcel summary in status requested. */
fun newTieredParcelSummary(code: String, name: String): TieredParcelSummary {
    return TieredParcelSummary(
        code = code,
        name = name,
        windowDays = 0,
        routeCode = "",
        updatedAt = 0,
        status = TieredParcelSummaryStatus.REQUESTED,
    )
}
