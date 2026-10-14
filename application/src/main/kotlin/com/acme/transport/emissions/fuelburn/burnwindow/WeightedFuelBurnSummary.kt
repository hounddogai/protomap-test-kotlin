package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted fuel burn summary is in its life cycle. */
enum class WeightedFuelBurnSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted fuel burn summary. Its code is unique within a tenant. */
data class WeightedFuelBurnSummary(
    var code: String,
    var label: String,
    var region: String,
    var retries: Int,
    var shiftCode: String,
    var contactEmail: String,
    var carrier: String,
    var driverId: String,
    var minimum: Double,
    var depot: String,
    var name: String,
    var status: WeightedFuelBurnSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnSummaryStatus): Boolean {
        when (status) {
            WeightedFuelBurnSummaryStatus.PROPOSED -> return next == WeightedFuelBurnSummaryStatus.REJECTED || next == WeightedFuelBurnSummaryStatus.WITHDRAWN
            WeightedFuelBurnSummaryStatus.APPROVED -> {
                when (next) {
                    WeightedFuelBurnSummaryStatus.REJECTED, WeightedFuelBurnSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedFuelBurnSummaryStatus.REJECTED -> {
                when (next) {
                    WeightedFuelBurnSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted fuel burn summary"} $code"

    fun effectiveLoad(): Double = round((minimum - retries.toDouble()) * 100) / 100

    fun snapshot(): WeightedFuelBurnSummary {
        return WeightedFuelBurnSummary(
            code = code,
            label = label,
            region = region,
            retries = retries,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            carrier = carrier,
            driverId = driverId,
            minimum = minimum,
            depot = depot,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted fuel burn summary in status proposed. */
fun newWeightedFuelBurnSummary(code: String, label: String): WeightedFuelBurnSummary {
    return WeightedFuelBurnSummary(
        code = code,
        label = label,
        region = "",
        retries = 0,
        shiftCode = "",
        contactEmail = "",
        carrier = "",
        driverId = "",
        minimum = 0.0,
        depot = "",
        name = "",
        status = WeightedFuelBurnSummaryStatus.PROPOSED,
    )
}
