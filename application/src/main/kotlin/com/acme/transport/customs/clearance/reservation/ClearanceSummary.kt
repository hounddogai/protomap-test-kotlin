package com.acme.transport.customs.clearance.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance summary is in its life cycle. */
enum class ClearanceSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A clearance summary as the clearance team keeps it. */
data class ClearanceSummary(
    var code: String,
    var name: String,
    var description: String,
    var cost: Double,
    var dock: String,
    var etaMinutes: Int,
    var label: String,
    var vehicleId: String,
    var batchSize: Int,
    var unit: String,
    var attempts: Int,
    var status: ClearanceSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceSummaryStatus): Boolean {
        when (status) {
            ClearanceSummaryStatus.CREATED -> {
                when (next) {
                    ClearanceSummaryStatus.VALIDATED, ClearanceSummaryStatus.PUBLISHED -> return true
                }
                return false
            }
            ClearanceSummaryStatus.VALIDATED -> return next == ClearanceSummaryStatus.PUBLISHED
            ClearanceSummaryStatus.PUBLISHED -> return next == ClearanceSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ClearanceSummaryStatus) {
        if (!canMoveTo(next)) {
            error("clearance summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((attempts.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ClearanceSummary {
        return ClearanceSummary(
            code = code,
            name = name,
            description = description,
            cost = cost,
            dock = dock,
            etaMinutes = etaMinutes,
            label = label,
            vehicleId = vehicleId,
            batchSize = batchSize,
            unit = unit,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a clearance summary in status created. */
fun newClearanceSummary(code: String, name: String): ClearanceSummary {
    return ClearanceSummary(
        code = code,
        name = name,
        description = "",
        cost = 0.0,
        dock = "",
        etaMinutes = 0,
        label = "",
        vehicleId = "",
        batchSize = 0,
        unit = "",
        attempts = 0,
        status = ClearanceSummaryStatus.CREATED,
    )
}
