package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict fuel surcharge summary records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a strict fuel surcharge summary is in its life cycle. */
enum class StrictFuelSurchargeSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict fuel surcharge summary as the fuel surcharge team keeps it. */
data class StrictFuelSurchargeSummary(
    var code: String,
    var title: String,
    var archived: Boolean,
    var priority: Int,
    var label: String,
    var distanceKm: Double,
    var country: String,
    var status: StrictFuelSurchargeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictFuelSurchargeSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictFuelSurchargeSummaryStatus): Boolean {
        when (status) {
            StrictFuelSurchargeSummaryStatus.NEW -> {
                when (next) {
                    StrictFuelSurchargeSummaryStatus.QUEUED -> return true
                }
                return false
            }
            StrictFuelSurchargeSummaryStatus.QUEUED -> {
                when (next) {
                    StrictFuelSurchargeSummaryStatus.DONE, StrictFuelSurchargeSummaryStatus.FAILED -> return true
                }
                return false
            }
            StrictFuelSurchargeSummaryStatus.RUNNING -> return next == StrictFuelSurchargeSummaryStatus.DONE || next == StrictFuelSurchargeSummaryStatus.FAILED
            StrictFuelSurchargeSummaryStatus.DONE -> return next == StrictFuelSurchargeSummaryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictFuelSurchargeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("strict fuel surcharge summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict fuel surcharge summary"} $code"

    fun projectedExposure(): Double = round((priority.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): StrictFuelSurchargeSummary {
        return StrictFuelSurchargeSummary(
            code = code,
            title = title,
            archived = archived,
            priority = priority,
            label = label,
            distanceKm = distanceKm,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict fuel surcharge summary in status new. */
fun newStrictFuelSurchargeSummary(code: String, title: String): StrictFuelSurchargeSummary {
    return StrictFuelSurchargeSummary(
        code = code,
        title = title,
        archived = false,
        priority = 0,
        label = "",
        distanceKm = 0.0,
        country = "",
        status = StrictFuelSurchargeSummaryStatus.NEW,
    )
}
