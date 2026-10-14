package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered declaration summary is in its life cycle. */
enum class TieredDeclarationSummaryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered declaration summary as the declaration team keeps it. */
data class TieredDeclarationSummary(
    var code: String,
    var label: String,
    var odometer: Int,
    var trailerId: String,
    var floorValue: Double,
    var category: String,
    var threshold: Double,
    var status: TieredDeclarationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationSummaryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationSummaryStatus): Boolean {
        when (status) {
            TieredDeclarationSummaryStatus.IDLE -> return next == TieredDeclarationSummaryStatus.STOPPED
            TieredDeclarationSummaryStatus.WARMING -> {
                when (next) {
                    TieredDeclarationSummaryStatus.DRAINING -> return true
                }
                return false
            }
            TieredDeclarationSummaryStatus.READY -> return next == TieredDeclarationSummaryStatus.DRAINING || next == TieredDeclarationSummaryStatus.STOPPED
            TieredDeclarationSummaryStatus.DRAINING -> {
                when (next) {
                    TieredDeclarationSummaryStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredDeclarationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round((floorValue - threshold) * 100) / 100

    fun snapshot(): TieredDeclarationSummary {
        return TieredDeclarationSummary(
            code = code,
            label = label,
            odometer = odometer,
            trailerId = trailerId,
            floorValue = floorValue,
            category = category,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered declaration summary in status idle. */
fun newTieredDeclarationSummary(code: String, label: String): TieredDeclarationSummary {
    return TieredDeclarationSummary(
        code = code,
        label = label,
        odometer = 0,
        trailerId = "",
        floorValue = 0.0,
        category = "",
        threshold = 0.0,
        status = TieredDeclarationSummaryStatus.IDLE,
    )
}
