package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.text.normalize

/** Where a tariff rollup is in its life cycle. */
enum class TariffRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One tariff rollup, with what the planners need to act on it. */
data class TariffRollup(
    var code: String,
    var label: String,
    var externalRef: String,
    var driverId: String,
    var dock: String,
    var amount: Double,
    var status: TariffRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffRollupStatus): Boolean {
        when (status) {
            TariffRollupStatus.NEW -> return next == TariffRollupStatus.RUNNING
            TariffRollupStatus.QUEUED -> return next == TariffRollupStatus.RUNNING || next == TariffRollupStatus.DONE
            TariffRollupStatus.RUNNING -> {
                when (next) {
                    TariffRollupStatus.DONE -> return true
                }
                return false
            }
            TariffRollupStatus.DONE -> return next == TariffRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TariffRollupStatus) {
        if (!canMoveTo(next)) {
            error("tariff rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tariff rollup"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): TariffRollup {
        return TariffRollup(
            code = code,
            label = label,
            externalRef = externalRef,
            driverId = driverId,
            dock = dock,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tariff rollup in status new. */
fun newTariffRollup(code: String, label: String): TariffRollup {
    return TariffRollup(
        code = code,
        label = label,
        externalRef = "",
        driverId = "",
        dock = "",
        amount = 0.0,
        status = TariffRollupStatus.NEW,
    )
}
