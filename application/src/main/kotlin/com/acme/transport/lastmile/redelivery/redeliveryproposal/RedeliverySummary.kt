package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery summary is in its life cycle. */
enum class RedeliverySummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a redelivery summary and its current status. */
data class RedeliverySummary(
    var code: String,
    var label: String,
    var unit: String,
    var shiftCode: String,
    var active: Boolean,
    var windowDays: Int,
    var country: String,
    var locale: String,
    var rate: Double,
    var status: RedeliverySummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliverySummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliverySummaryStatus): Boolean {
        when (status) {
            RedeliverySummaryStatus.NEW -> {
                when (next) {
                    RedeliverySummaryStatus.FAILED -> return true
                }
                return false
            }
            RedeliverySummaryStatus.QUEUED -> {
                when (next) {
                    RedeliverySummaryStatus.RUNNING, RedeliverySummaryStatus.FAILED -> return true
                }
                return false
            }
            RedeliverySummaryStatus.RUNNING -> return next == RedeliverySummaryStatus.DONE || next == RedeliverySummaryStatus.FAILED
            RedeliverySummaryStatus.DONE -> return next == RedeliverySummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RedeliverySummaryStatus) {
        if (!canMoveTo(next)) {
            error("redelivery summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveValue(): Double = round(rate * windowDays.toDouble() * 100) / 100

    fun snapshot(): RedeliverySummary {
        return RedeliverySummary(
            code = code,
            label = label,
            unit = unit,
            shiftCode = shiftCode,
            active = active,
            windowDays = windowDays,
            country = country,
            locale = locale,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery summary in status new. */
fun newRedeliverySummary(code: String, label: String): RedeliverySummary {
    return RedeliverySummary(
        code = code,
        label = label,
        unit = "",
        shiftCode = "",
        active = false,
        windowDays = 0,
        country = "",
        locale = "",
        rate = 0.0,
        status = RedeliverySummaryStatus.NEW,
    )
}
