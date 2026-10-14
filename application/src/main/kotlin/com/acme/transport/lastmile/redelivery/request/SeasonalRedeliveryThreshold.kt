package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal redelivery threshold is in its life cycle. */
enum class SeasonalRedeliveryThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal redelivery threshold. Its code is unique within a tenant. */
data class SeasonalRedeliveryThreshold(
    var code: String,
    var name: String,
    var unit: String,
    var loadKg: Double,
    var dock: String,
    var description: String,
    var weight: Double,
    var startsAt: Int,
    var amount: Double,
    var visible: Boolean,
    var status: SeasonalRedeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRedeliveryThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRedeliveryThresholdStatus): Boolean {
        when (status) {
            SeasonalRedeliveryThresholdStatus.NEW -> {
                when (next) {
                    SeasonalRedeliveryThresholdStatus.RUNNING -> return true
                }
                return false
            }
            SeasonalRedeliveryThresholdStatus.QUEUED -> {
                when (next) {
                    SeasonalRedeliveryThresholdStatus.RUNNING, SeasonalRedeliveryThresholdStatus.DONE -> return true
                }
                return false
            }
            SeasonalRedeliveryThresholdStatus.RUNNING -> return next == SeasonalRedeliveryThresholdStatus.DONE || next == SeasonalRedeliveryThresholdStatus.FAILED
            SeasonalRedeliveryThresholdStatus.DONE -> {
                when (next) {
                    SeasonalRedeliveryThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRedeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("seasonal redelivery threshold $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal redelivery threshold"} $code"

    fun projectedTotal(): Double = round(amount * startsAt.toDouble() * 100) / 100

    fun snapshot(): SeasonalRedeliveryThreshold {
        return SeasonalRedeliveryThreshold(
            code = code,
            name = name,
            unit = unit,
            loadKg = loadKg,
            dock = dock,
            description = description,
            weight = weight,
            startsAt = startsAt,
            amount = amount,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal redelivery threshold in status new. */
fun newSeasonalRedeliveryThreshold(code: String, name: String): SeasonalRedeliveryThreshold {
    return SeasonalRedeliveryThreshold(
        code = code,
        name = name,
        unit = "",
        loadKg = 0.0,
        dock = "",
        description = "",
        weight = 0.0,
        startsAt = 0,
        amount = 0.0,
        visible = false,
        status = SeasonalRedeliveryThresholdStatus.NEW,
    )
}
