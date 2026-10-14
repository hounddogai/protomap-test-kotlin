package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker summary is in its life cycle. */
enum class LockerSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A locker summary. Its code is unique within a tenant. */
data class LockerSummary(
    var code: String,
    var name: String,
    var active: Boolean,
    var score: Double,
    var priority: Int,
    var timezone: String,
    var routeCode: String,
    var shiftCode: String,
    var status: LockerSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerSummaryStatus): Boolean {
        when (status) {
            LockerSummaryStatus.DRAFT -> return next == LockerSummaryStatus.CLOSED
            LockerSummaryStatus.PENDING -> {
                when (next) {
                    LockerSummaryStatus.ACTIVE, LockerSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            LockerSummaryStatus.ACTIVE -> return next == LockerSummaryStatus.SUSPENDED || next == LockerSummaryStatus.CLOSED
            LockerSummaryStatus.SUSPENDED -> return next == LockerSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LockerSummaryStatus) {
        if (!canMoveTo(next)) {
            error("locker summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker summary"} $code"

    fun weightedTotal(): Double = round(score * priority.toDouble() * 100) / 100

    fun snapshot(): LockerSummary {
        return LockerSummary(
            code = code,
            name = name,
            active = active,
            score = score,
            priority = priority,
            timezone = timezone,
            routeCode = routeCode,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a locker summary in status draft. */
fun newLockerSummary(code: String, name: String): LockerSummary {
    return LockerSummary(
        code = code,
        name = name,
        active = false,
        score = 0.0,
        priority = 0,
        timezone = "",
        routeCode = "",
        shiftCode = "",
        status = LockerSummaryStatus.DRAFT,
    )
}
