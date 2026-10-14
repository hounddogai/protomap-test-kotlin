package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tariff exception is in its life cycle. */
enum class PrimaryTariffExceptionStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary tariff exception, with what the planners need to act on it. */
data class PrimaryTariffException(
    var code: String,
    var title: String,
    var graceDays: Int,
    var discount: Double,
    var distanceKm: Double,
    var status: PrimaryTariffExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTariffExceptionStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTariffExceptionStatus): Boolean {
        when (status) {
            PrimaryTariffExceptionStatus.OPEN -> {
                when (next) {
                    PrimaryTariffExceptionStatus.IN_REVIEW, PrimaryTariffExceptionStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryTariffExceptionStatus.IN_REVIEW -> return next == PrimaryTariffExceptionStatus.RESOLVED || next == PrimaryTariffExceptionStatus.ARCHIVED
            PrimaryTariffExceptionStatus.RESOLVED -> return next == PrimaryTariffExceptionStatus.REOPENED || next == PrimaryTariffExceptionStatus.ARCHIVED
            PrimaryTariffExceptionStatus.REOPENED -> return next == PrimaryTariffExceptionStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryTariffExceptionStatus) {
        if (!canMoveTo(next)) {
            error("primary tariff exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveVolume(): Double = round((distanceKm - graceDays.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTariffException {
        return PrimaryTariffException(
            code = code,
            title = title,
            graceDays = graceDays,
            discount = discount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary tariff exception in status open. */
fun newPrimaryTariffException(code: String, title: String): PrimaryTariffException {
    return PrimaryTariffException(
        code = code,
        title = title,
        graceDays = 0,
        discount = 0.0,
        distanceKm = 0.0,
        status = PrimaryTariffExceptionStatus.OPEN,
    )
}
