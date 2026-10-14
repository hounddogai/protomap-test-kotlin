package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel surcharge forecast is in its life cycle. */
enum class GlobalFuelSurchargeForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a global fuel surcharge forecast and its current status. */
data class GlobalFuelSurchargeForecast(
    var code: String,
    var label: String,
    var dock: String,
    var etaMinutes: Int,
    var capacity: Int,
    var source: String,
    var revision: Int,
    var status: GlobalFuelSurchargeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeForecastStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeForecastStatus.PLANNED -> return next == GlobalFuelSurchargeForecastStatus.SCHEDULED || next == GlobalFuelSurchargeForecastStatus.COMPLETED
            GlobalFuelSurchargeForecastStatus.SCHEDULED -> {
                when (next) {
                    GlobalFuelSurchargeForecastStatus.IN_PROGRESS, GlobalFuelSurchargeForecastStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalFuelSurchargeForecastStatus.IN_PROGRESS -> return next == GlobalFuelSurchargeForecastStatus.COMPLETED || next == GlobalFuelSurchargeForecastStatus.CANCELLED
            GlobalFuelSurchargeForecastStatus.COMPLETED -> return next == GlobalFuelSurchargeForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeForecastStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge forecast $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun netTotal(): Double = round(revision.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): GlobalFuelSurchargeForecast {
        return GlobalFuelSurchargeForecast(
            code = code,
            label = label,
            dock = dock,
            etaMinutes = etaMinutes,
            capacity = capacity,
            source = source,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global fuel surcharge forecast in status planned. */
fun newGlobalFuelSurchargeForecast(code: String, label: String): GlobalFuelSurchargeForecast {
    return GlobalFuelSurchargeForecast(
        code = code,
        label = label,
        dock = "",
        etaMinutes = 0,
        capacity = 0,
        source = "",
        revision = 0,
        status = GlobalFuelSurchargeForecastStatus.PLANNED,
    )
}
