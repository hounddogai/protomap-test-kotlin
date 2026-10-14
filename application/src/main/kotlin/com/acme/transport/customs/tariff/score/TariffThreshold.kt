package com.acme.transport.customs.tariff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff threshold is in its life cycle. */
enum class TariffThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tariff threshold as the tariff team keeps it. */
data class TariffThreshold(
    var code: String,
    var name: String,
    var retries: Int,
    var source: String,
    var dock: String,
    var category: String,
    var ratio: Double,
    var status: TariffThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffThresholdStatus): Boolean {
        when (status) {
            TariffThresholdStatus.IDLE -> return next == TariffThresholdStatus.WARMING || next == TariffThresholdStatus.DRAINING
            TariffThresholdStatus.WARMING -> {
                when (next) {
                    TariffThresholdStatus.READY -> return true
                }
                return false
            }
            TariffThresholdStatus.READY -> return next == TariffThresholdStatus.DRAINING
            TariffThresholdStatus.DRAINING -> return next == TariffThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TariffThresholdStatus) {
        if (!canMoveTo(next)) {
            error("tariff threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tariff threshold"} $code"

    fun weightedValue(): Double = round((ratio + retries.toDouble()) * 100) / 100

    fun snapshot(): TariffThreshold {
        return TariffThreshold(
            code = code,
            name = name,
            retries = retries,
            source = source,
            dock = dock,
            category = category,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tariff threshold in status idle. */
fun newTariffThreshold(code: String, name: String): TariffThreshold {
    return TariffThreshold(
        code = code,
        name = name,
        retries = 0,
        source = "",
        dock = "",
        category = "",
        ratio = 0.0,
        status = TariffThresholdStatus.IDLE,
    )
}
