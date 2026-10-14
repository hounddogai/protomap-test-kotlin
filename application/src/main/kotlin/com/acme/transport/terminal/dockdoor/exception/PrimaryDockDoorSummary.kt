package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary dock door summary is in its life cycle. */
enum class PrimaryDockDoorSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary dock door summary and its current status. */
data class PrimaryDockDoorSummary(
    var code: String,
    var title: String,
    var startsAt: Int,
    var locale: String,
    var ratio: Double,
    var status: PrimaryDockDoorSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDockDoorSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDockDoorSummaryStatus): Boolean {
        when (status) {
            PrimaryDockDoorSummaryStatus.REQUESTED -> return next == PrimaryDockDoorSummaryStatus.EXPIRED
            PrimaryDockDoorSummaryStatus.GRANTED -> {
                when (next) {
                    PrimaryDockDoorSummaryStatus.REVOKED, PrimaryDockDoorSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryDockDoorSummaryStatus.REVOKED -> {
                when (next) {
                    PrimaryDockDoorSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDockDoorSummaryStatus) {
        if (!canMoveTo(next)) {
            error("primary dock door summary $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((ratio + startsAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryDockDoorSummary {
        return PrimaryDockDoorSummary(
            code = code,
            title = title,
            startsAt = startsAt,
            locale = locale,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary dock door summary in status requested. */
fun newPrimaryDockDoorSummary(code: String, title: String): PrimaryDockDoorSummary {
    return PrimaryDockDoorSummary(
        code = code,
        title = title,
        startsAt = 0,
        locale = "",
        ratio = 0.0,
        status = PrimaryDockDoorSummaryStatus.REQUESTED,
    )
}
