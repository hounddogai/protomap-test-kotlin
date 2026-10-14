package com.acme.transport.customs.tariff.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tariff exception is in its life cycle. */
enum class TariffExceptionStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tariff exception, with what the planners need to act on it. */
data class TariffException(
    var code: String,
    var title: String,
    var pallets: Int,
    var batchSize: Int,
    var ownerId: String,
    var currency: String,
    var status: TariffExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffExceptionStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffExceptionStatus): Boolean {
        when (status) {
            TariffExceptionStatus.IDLE -> return next == TariffExceptionStatus.WARMING || next == TariffExceptionStatus.READY
            TariffExceptionStatus.WARMING -> {
                when (next) {
                    TariffExceptionStatus.DRAINING -> return true
                }
                return false
            }
            TariffExceptionStatus.READY -> {
                when (next) {
                    TariffExceptionStatus.STOPPED -> return true
                }
                return false
            }
            TariffExceptionStatus.DRAINING -> {
                when (next) {
                    TariffExceptionStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffExceptionStatus) {
        if (!canMoveTo(next)) {
            error("tariff exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((pallets.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): TariffException {
        return TariffException(
            code = code,
            title = title,
            pallets = pallets,
            batchSize = batchSize,
            ownerId = ownerId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff exception in status idle. */
fun newTariffException(code: String, title: String): TariffException {
    return TariffException(
        code = code,
        title = title,
        pallets = 0,
        batchSize = 0,
        ownerId = "",
        currency = "",
        status = TariffExceptionStatus.IDLE,
    )
}
